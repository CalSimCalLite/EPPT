!     Last change:  AM    3 Dec 1999    4:56 pm
!     Copyright (C) 1998, 2000 State of California, Department of Water
!     Resources.

!     This program is licensed to you under the terms of the GNU General
!     Public License, version 2, as published by the Free Software
!     Foundation.

!     You should have received a copy of the GNU General Public License
!     along with this program; if not, contact Dr. Sushil Arora, below,
!     or the Free Software Foundation, 675 Mass Ave, Cambridge, MA
!     02139, USA.

!     THIS SOFTWARE AND DOCUMENTATION ARE PROVIDED BY THE CALIFORNIA
!     DEPARTMENT OF WATER RESOURCES AND CONTRIBUTORS "AS IS" AND ANY
!     EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
!     IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
!     PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE CALIFORNIA
!     DEPARTMENT OF WATER RESOURCES OR ITS CONTRIBUTORS BE LIABLE FOR
!     ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
!     CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
!     OR SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR PROFITS; OR
!     BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
!     LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
!     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
!     USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
!     DAMAGE.

!     For more information, contact:

!     Dr. Sushil Arora
!     California Dept. of Water Resources
!     Division of Planning, Delta Modeling Section
!     1416 Ninth Street
!     Sacramento, CA  95814
!     916-653-7921
!     sushil@water.ca.gov


!    This module provides the low-level functionality for DSS data I/O.
!    Written by Dudley McFadden (February 1998)
!    You use this module by creating one or more TYPE(VariableCaches)
!    then using the public procedures to manipulated these caches

module dss_cache
  implicit none
  PUBLIC

  INTEGER, PARAMETER                  :: DVAR_LEN = 16, header_len = 117
  INTEGER, parameter                  :: kiwi = 32, datelen = 20
  INTEGER,PARAMETER,PRIVATE           :: message_unit = 14
  CHARACTER(LEN=200),private          :: msg
  CHARACTER(LEN=8),PARAMETER,private  :: CTYPE='PER-AVER'
  LOGICAL, PRIVATE                    :: flushRequired, debug=.true.  !CB set it to false as default

  ! array to store the sequence order of variables from XA
!CB  INTEGER, DIMENSION(10000), SAVE :: dvar_order=0   !CB if too small, Windows stops the exe file ************* CalSim 3 needs it BIG!
  INTEGER, DIMENSION(50000), SAVE :: dvar_order=0   !CB if too small, Windows stops the exe file ************* CalSim 3 needs it BIG!

  ! Variable value caches
  TYPE SingleVariableCache
     CHARACTER(LEN=KIWI)            :: NAME, KIND, DESC   ! b-part, c-part, and f-part
     CHARACTER(LEN=8)               :: UNITS
     REAL, DIMENSION(:), POINTER    :: GOODS          ! Will be sized the same for each variable
  END TYPE SingleVariableCache
  TYPE VariableCache
     TYPE(SingleVariableCache), DIMENSION(:), POINTER :: tab
     LOGICAL        					:: isDvarCache                  ! true if Dvar cache otherwise false
     CHARACTER(LEN=datelen)           :: cache_date                  ! base date of the caches
     INTEGER                          :: current_offset              ! position of simulation%current_date in this cache
  END TYPE VariableCache

  ! Simulation state information
  TYPE Info
     private
     CHARACTER(LEN=32)      :: apart, fpart, timestep
     CHARACTER(LEN=datelen) :: current_date
  END type Info
  TYPE(Info) :: simulation



contains

  ! Access method for indicating that unsaved data awaits writing.
  subroutine setFlushRequired()
    flushRequired = .true.
  end subroutine setFlushRequired


  ! Access method that activates debugging output
  subroutine setDebug()
    debug = .true.
  end subroutine setDebug


  ! Deallocates a cache.  First deallocate the data, then the cache metadata (and itself)
  subroutine killOff(cache)
    TYPE(VariableCache), INTENT(INOUT) :: cache
    INTEGER :: i, is
    do i=1, UBOUND(cache%tab,1)
       DEALLOCATE(cache%tab(i)%goods, STAT=is)
    END do
    deallocate(cache%tab, STAT=is)
    is=is   ! ignore the return code
  end subroutine killOff


  ! Wrapper around the HEC subroutine.  Traps most errors.
  subroutine zzopen(cname,ifltab)
    USE HEC_LIB_DSS
    CHARACTER(LEN=*),INTENT(IN)          :: cname
    INTEGER, DIMENSION(600),INTENT(INOUT):: ifltab
    INTEGER              :: ios
101 FORMAT("Can't open ",a,", status ",i4)
    call zopen(ifltab, cname, ios)
    if (ios.ne.0) then
       WRITE(msg,101) TRIM(cname), ios
       call stopWithError()
    end if
  end subroutine zzopen


  ! Close DSS files
  subroutine zzclose(ifltab)
    USE HEC_LIB_DSS
    INTEGER, DIMENSION(600),INTENT(INOUT):: ifltab
    call zclose(ifltab)
  end subroutine zzclose


  ! Verifies pathname for legality.  Only returns if it is okay.
  subroutine verifypath(path)
    USE HEC_LIB_DSS
    CHARACTER(LEN=*),INTENT(IN):: path
    CHARACTER(LEN=50)          :: reason
    INTEGER                    :: istat

100 FORMAT('Pathname: ',a,a,a)
    call zchkpn(path,LEN_TRIM(path),istat)
    if (istat/=0) then
       IF (istat==-1) then
          reason = 'First char not slash'
       ELSE IF (istat==-2) then
          reason = 'Last char not slash'
       ELSE IF (istat==-3) then
          reason = 'Need 7 slashes'
       ELSE IF (istat==-4) then
          reason = 'too short'
       ELSE IF (istat==-5) then
          reason = 'too long'
       ELSE IF (istat==-6) then
          reason = 'First char not /'
       else
          reason = ' '
       END if
       WRITE(msg,100) TRIM(reason), CHAR(13), TRIM(path)
       call stopWithError()
    end if
    
    if(debug) WRITE(message_unit,100) TRIM(path)
  END subroutine verifypath


  ! Sets up the base pathname.  Examines it and extracts simulation parameters.
  subroutine initSimulation(path)
    USE HEC_LIB_DSS
    implicit none
    CHARACTER(LEN=32) :: ca,cb,cc,cd,ce,cf
    INTEGER :: na,nb,nc,nd,ne,nf, npath
    INTEGER :: istat
    CHARACTER(LEN=*),INTENT(IN)    :: path
    CHARACTER(LEN=80)              :: cpath

    open(message_unit,FILE='wranglerDSS.log',STATUS='UNKNOWN',ACTION='DENYWRITE')
    WRITE(message_unit,*) 'Log Open.'
    call zset('MUNI','',message_unit)
!    if (.not.debug) call zset('MLEVEL','',0)
    cpath = ADJUSTL(path)
    call verifypath(cpath)
    ! Now, find the model time step and start date from the pathname
    npath = len_trim(cpath)
    call zufpn(ca,na,cb,nb,cc,nc,cd,nd,ce,ne,cf,nf,cpath,npath,istat)
    if (istat/=0) then
       msg='Error at zufpn in initSimulation!'
       call stopWithError()
    end if
    simulation%timestep = uc(ce)
    simulation%apart = uc(ca)
    simulation%fpart =  uc(cf)
    simulation%current_date  = uc(cd)
    
    ! Set up DSS options
    call zset("PROG","WRIMS",istat)
    IF(debug) call zset("MLEVEL","",8)
  end subroutine initSimulation


  ! Adds a variable to this cache.  Specify name, kind, units, description.
  subroutine addVariable(pos, table, vname, vkind, vunits, vdesc)
    TYPE(SingleVariableCache), INTENT(INOUT)  :: table(:)
    CHARACTER(LEN=32),INTENT(IN)   :: vname, vkind, vunits, vdesc
    OPTIONAL                       :: vdesc
    INTEGER, INTENT(IN) :: pos
    INTEGER :: i

101 FORMAT("DSS Path Size Exceeded! Reduce characters for Variable:",a," or Kind:"a)
    IF(debug) WRITE( message_unit,*) 'variable, ', table(pos)%NAME, '  was added to the cache'  !CB DEBUGGING
!    WRITE(*,*) 'variable, ', table(pos)%NAME, '  was added to the cache'  !DEBUGGING
    table(pos)%NAME  = uc(vname)
    table(pos)%KIND  = uc(vkind)
    table(pos)%UNITS = uc(vunits)

    ! If they specified an F-part, use it, otherwise use the default F-part
    if (PRESENT(vdesc)) then
       table(pos)%DESC = uc(vdesc)
    else
       table(pos)%DESC = simulation%fpart
    end if
    i=LEN_TRIM('CALSIM') 					!A-PART
    i=i+LEN_TRIM(table(pos)%NAME) !B-PART   
    i=i+LEN_TRIM(table(pos)%KIND) !C-PART
    i=i+LEN_TRIM(table(pos)%DESC) !F-PART
    i=i+20                        !D,E-PARTS,slash chars
    if (i>80) then ! maximum dss path length
       WRITE(msg,101) TRIM(table(pos)%NAME), TRIM(table(pos)%KIND)
       call stopWithError()
    end if

  END subroutine addVariable

  ! Create storage for the data in a cache.  Allocate an array for each of the
  ! variables defined for the cache.
  subroutine allocateStorage(cache, nbr_periods)
    TYPE(VariableCache), INTENT(INOUT):: cache
    INTEGER, INTENT(IN)               :: nbr_periods
    INTEGER :: i, amount_needed

    amount_needed = ABS(nbr_periods)
    do i=1,UBOUND(cache%tab,1)
       IF (ASSOCIATED( cache%tab(i)%goods)) then
          IF (amount_needed /= SIZE(cache%tab(i)%goods)) then
             DEALLOCATE(cache%tab(i)%goods)
             ALLOCATE(cache%tab(i)%goods(amount_needed))
          END if
       else
          ALLOCATE(cache%tab(i)%goods(amount_needed))
       END if
       cache%tab(i)%goods=-902.
    end do
  end subroutine allocateStorage


  ! Clears data in the cache in the given date range by setting to -902.
  ! The date range may extend beyond the end of the cache.
  subroutine clearValues( ifltab, cache, header, cdate, nvals)
    USE hec_lib_dss
    TYPE(VariableCache), INTENT(IN)   :: cache
    CHARACTER(LEN=datelen),INTENT(IN) :: cdate
    INTEGER, DIMENSION(600)           :: ifltab
    INTEGER                           :: nvals, istat, i, nheadu
    CHARACTER(LEN=4)                  :: ctime = '2400'
    INTEGER,parameter                 :: kheadu = 32
    REAL :: flags(1), headu(kheadu)
    REAL, DIMENSION(:), ALLOCATABLE   :: values
    CHARACTER(LEN=header_len), DIMENSION(:), INTENT(IN) :: header
    INTENT(IN) :: nvals
    
!    WRITE (*,*) 'DV dss solution data being deleted - clearValues'  !DEBUGGING

    ALLOCATE( values( nvals))
    values = -902.
    do i=1, UBOUND(cache%tab,1)
       if (LEN_TRIM(header(i)) > 0) then
          nheadu=0
          call zstfh((/'CALSIM INFO'/),header(i),1,headu, kheadu, nheadu, istat)
          call zsrtsx(ifltab, &
               formPathname(cache%tab(i)), &
               cdate, &
               ctime, nvals, values, &
               flags, .false., &
               cache%tab(i)%units, CTYPE, &
               headu, kheadu, 0, &          ! NOT nheadu
               0, 0., .FALSE., .FALSE., 0, istat)
       else
          call zsrts(ifltab, &
               formPathname(cache%tab(i)), &
               cdate, &
               ctime, nvals, values, &
               cache%tab(i)%units, CTYPE, 0, istat)
       end if
       if (istat/=0) then
          call dssErrorMessage(istat)
          call stopWithError
       end if
    end do
    DEALLOCATE( values)
  end subroutine clearValues


  ! Writes out the contents of the cache.  It writes out the entire thing, even the
  ! early part that didn't change.  Reason: simplicity, and also, most of the time
  ! the DSS low-level subsystem does this anyhow because data is stored in decade blocks.
  subroutine flushOut(ifltab, cache, header, per_offset)
    USE hec_lib_dss
    TYPE(VariableCache), INTENT(INOUT):: cache
    INTEGER, DIMENSION(600)           :: ifltab
    INTEGER                           :: nvals, istat, i, nheadu
    CHARACTER(LEN=4)                  :: ctime = '2400'
    INTEGER,parameter                 :: kheadu = 32
    REAL :: flags(1), headu(kheadu)
    CHARACTER(LEN=header_len), DIMENSION(:), INTENT(IN) :: header
    INTEGER, OPTIONAL, INTENT(IN) :: per_offset

!    WRITE (*,*) 'DV dss solution data being deleted - flushOut'  !DEBUGGING

    if (.not. flushRequired) then
       IF(debug) WRITE( message_unit,*) 'flushOut: flush not required, so no data will be written'
       return
    else
       IF(debug) WRITE( message_unit,*) 'flushOut: flushing these many items:', SIZE(cache%tab)
    END if

    if (PRESENT(per_offset)) then
	    do i=1, UBOUND(cache%tab,1)
  	     nvals = UBOUND(cache%tab(i)%goods(1+per_offset:),1) 
    	   if (LEN_TRIM(header(i)) > 0) then
      	    nheadu=0
	          call zstfh((/'CALSIM INFO'/),header(i),1,headu, kheadu, nheadu, istat)
  	        call zsrtsx(ifltab, &
	               formPathname(cache%tab(i)), &
	               offsetDate(cache%cache_date,per_offset), &
	               ctime, nvals, cache%tab(i)%goods(1+per_offset:), &
	               flags, .false., &
	               cache%tab(i)%units, CTYPE, &
	               headu, kheadu, 0, &          ! NOT nheadu
	               0, 0., .FALSE., .FALSE., 0, istat)
	       else
	          call zsrts(ifltab, &
	               formPathname(cache%tab(i)), &
	               offsetDate(cache%cache_date,per_offset), &
	               ctime, nvals, cache%tab(i)%goods(1+per_offset:), &
	               cache%tab(i)%units, CTYPE, 0, istat)
	       end if
    	   if (istat/=0) then
      	    call dssErrorMessage(istat)
        	  call stopWithError
	       end if
  	  end do
    else
	    do i=1, UBOUND(cache%tab,1)
	       nvals = UBOUND(cache%tab(i)%goods,1)
	       if (LEN_TRIM(header(i)) > 0) then
	          nheadu=0
	          call zstfh((/'CALSIM INFO'/),header(i),1,headu, kheadu, nheadu, istat)
	          call zsrtsx(ifltab, &
	               formPathname(cache%tab(i)), &
	               cache%cache_date, &
	               ctime, nvals, cache%tab(i)%goods, &
	               flags, .false., &
	               cache%tab(i)%units, CTYPE, &
	               headu, kheadu, 0, &          ! NOT nheadu
	               0, 0., .FALSE., .FALSE., 0, istat)
	       else
	          call zsrts(ifltab, &
	               formPathname(cache%tab(i)), &
	               cache%cache_date, &
	               ctime, nvals, cache%tab(i)%goods, &
	               cache%tab(i)%units, CTYPE, 0, istat)
	       end if
    	   if (istat/=0) then
      	    call dssErrorMessage(istat)
        	  call stopWithError
	       end if
  	  end do
    end if
    flushRequired = .false.
  end subroutine flushOut


  ! Fill a cache with data from a DSS database.  The amount of data read is determined
  ! by the dimensioned size of the cached variable arrays.
  subroutine readIn(ifltab, cache, okayIfMissing, fpart)
    USE HEC_LIB_DSS
    TYPE(VariableCache), INTENT(INOUT):: cache
    INTEGER, DIMENSION(600)           :: ifltab
    INTEGER                           :: nvals, istat, i, max_nvals
    CHARACTER(LEN=4)                  :: ctime = '2400'
    CHARACTER(LEN=8)                  :: units
    character(len=*),intent(in), optional :: fpart
    LOGICAL, INTENT(IN)               :: okayIfMissing
    
101 FORMAT(1x,'readIn (',i2,') ',a,': ',a,i4,' periods')
    do i=1, UBOUND(cache%tab,1)
       nvals = UBOUND(cache%tab(i)%goods,1)
       max_nvals = nvals
       IF(debug) WRITE(message_unit,101) i, cache%tab(i)%NAME, cache%cache_date, nvals
       call zrrts(ifltab, formPathname(cache%tab(i),fpart), cache%cache_date, ctime, &
            nvals, cache%tab(i)%goods, units, CTYPE, 0, istat)
       call checkForError(i)
       if (nvals < max_nvals) call warnIncompleteData(i)   ! note: it won't be greater
    end do
    return

  CONTAINS

    subroutine checkForError(i)
      INTEGER, INTENT(IN) :: i
100   FORMAT('Units mismatch (',a,'):',a,'DSS has ',a,', requested ',a)
104   FORMAT('No data for ',a,a,'Starting date: ',a)
105   FORMAT('Missing DSS record!',a,'Pathname: ',a,a,'Date: ',a)
      if (istat>5) then
         call dssErrorMessage(istat)
         call stopWithError
      end if
      if (istat==5 .and. .not. okayIfMissing) then
         WRITE(msg,105)  CHAR(13), formPathname(cache%tab(i)), CHAR(13), cache%cache_date
         call stopWithError()
      end if
      if (istat==4 .and. .not. okayIfMissing) then
         WRITE(msg,104) TRIM(cache%tab(i)%NAME), CHAR(13), cache%cache_date
         call stopWithError()
      end if
      if (istat<4 .and. cache%tab(i)%units .ne. 'UNKNOWN' .and. units /= cache%tab(i)%units) then
         WRITE(msg,100) TRIM(cache%tab(i)%NAME), CHAR(13), units, cache%tab(i)%units
         call stopWithError
      end if
      return
    end subroutine checkForError

    subroutine warnIncompleteData(i)
      INTEGER, INTENT(IN) :: i
      ! fill up remainder of cache with missing values
200   FORMAT('** WARNING ** Variable ',a,' has missing data beginning',i3, &
           ' periods beyond ',a)
      WRITE(message_unit,200) TRIM(cache%tab(i)%NAME), nvals+1, cache%cache_date
      cache%tab(i)%goods(nvals+1:max_nvals) = -902.   ! missing data
      return
    end subroutine warnIncompleteData

  end subroutine readIn
  ! Obtains a value from a cache
  ! This function is speed critical
  function getCachedValue(cache, pos, requested_offset) RESULT(desiredValue)
    REAL :: desiredValue
    TYPE(VariableCache), INTENT(INOUT):: cache
    INTEGER, INTENT(IN)               :: requested_offset, pos
    INTEGER :: pos_in_cache
100 FORMAT("** Warning: value for variable '",a,"' is missing in DSS file.  Date is",i5," time steps from ",a)
200 FORMAT("** Error:   Attempting to access future value of decision variable '",a,"' Date is",i5," time steps from ",a)
    pos_in_cache = requested_offset + cache%current_offset
    if (debug) then
       call whereWeAre('/')
       WRITE(message_unit,*)msg
    end if
    if (pos_in_cache <= 0 .or. pos_in_cache > UBOUND(cache%tab(pos)%goods,1)) then
       call whereWeAre(CHAR(13))
       call stopWithError
    end if
    desiredValue = cache%tab(pos)%goods(pos_in_cache)

    ! stop if trying to access future decision variable values
    if (cache%isDvarCache.and.requested_offset>-0.1) THEN
       call futureDvarMsg(CHAR(13))
       call stopWithError()
    end if
    ! Warn them if missing data was found in the cache
    if (approxEq(desiredValue,-901.) .or. approxEq(desiredValue,-902.)) then
       WRITE(message_unit,100) TRIM(cache%tab(pos)%NAME), requested_offset,simulation%current_date
       call whereWeAre(CHAR(13))
       call stopWithError()
    end if
    return
  contains
    function approxEq(x,y)
      real, intent(in) :: x,y
      real :: approx 
      logical :: approxEq
      approx = 1e-06
      if ( abs(x-y) .le. approx ) then 
         approxEq = .true.
      else
         approxEq = .false.
      end if
    end function approxEq
    subroutine whereWeAre(separator)
      CHARACTER(LEN=1) :: separator
101   FORMAT("Out of cache: number",I5, &
           a, "Variable : ",a, &
           a, "Requested ",i4," steps from ",a, "(pos: ",i2,")", &
           a, "Cache start : ",a, &
           a, "Cache size : ",i4," timesteps")
      WRITE(msg,101) pos, &
           separator, cache%tab(pos)%NAME, &
           separator, requested_offset, simulation%current_date, pos_in_cache,&
           separator, cache%cache_date, &
           separator, UBOUND(cache%tab(pos)%goods,1)
    end subroutine whereWeAre

    subroutine futureDvarMsg(separator)
      CHARACTER(LEN=1) :: separator
101   FORMAT("Future Decision Variable Access Attempted!", &
           a, "Decision Variable: ",a, &
           a, "Requested ",i4," steps from ",a, "(pos: ",i2,")" )
      WRITE(msg,101) separator, TRIM(cache%tab(pos)%NAME), &
           separator,requested_offset, simulation%current_date, pos_in_cache
    end subroutine futureDvarMsg

  end function getCachedValue


  ! Move cache pointers to the next time step
  subroutine incrementCache(cache)
    TYPE(VariableCache), INTENT(INOUT):: cache
100 FORMAT("Exceeding cached limit",a,"Date: ",a)
    ! if there are no variables in this cache, no need to increment
    if (ubound( cache%tab, 1) == 0) then
       IF(debug) WRITE( message_unit, *) 'incrementCache:  cache is empty'
       return
    END if
    cache%current_offset = cache%current_offset+1
    IF(debug) WRITE(message_unit, *) 'incrementCache:  offset is now', cache%current_offset
    IF(cache%current_offset > UBOUND(cache%tab(1)%goods,1)) then
       WRITE(msg,100) CHAR(13), simulation%current_date
       call stopWithError
    endif
  END subroutine incrementCache


  ! Routine to move LP solution from wrapper into the cache
  subroutine copyValues(cache, from_array, from_names)
    CHARACTER(LEN=DVAR_LEN), INTENT(in)    :: from_names(:)
    CHARACTER(LEN=KIWI)    						:: tmpname
    REAL(8),INTENT(in)                        :: from_array(:)
    TYPE(VariableCache), INTENT(INOUT)     :: cache
    INTEGER :: nbr, pos,i
100 FORMAT("CopyValues: dvar quantity mismatch",a,"Wrapper passed ",i4, " cache has",i4)
    nbr = SIZE(cache%tab)
    if (nbr < SIZE(from_array)) then
       WRITE(msg,100) CHAR(13), UBOUND(from_array,1), nbr
       call stopWithError
    end if
    if(debug) WRITE(message_unit,*) 'copyValues: saving this many decision variables:', nbr
    if(debug) call flush(message_unit)
    do i=1,SIZE(from_names)
       tmpname = from_names(i)
       tmpname = uc(tmpname)
       pos = dvar_order(i)
       if (pos==0) pos = findit(from_names(i), cache%tab%NAME)
       if (cache%tab(pos)%NAME/=tmpname) then
       	 pos = findit(from_names(i), cache%tab%NAME,pos)
       end if
       cache%tab(pos)%goods(cache%current_offset) = from_array(i)
       dvar_order(i) = pos
    end do
    if(debug) WRITE(message_unit,*) 'copyValues: complete'
    if(debug) call flush(message_unit)
    return

  contains

    ! Finds the postion (index) of string c amongst array
    function findit(c, array,sindex) RESULT (loc)
      CHARACTER(LEN=DVAR_LEN), INTENT(IN) :: c
      CHARACTER(LEN=kiwi),INTENT(IN):: array(:)
      CHARACTER(LEN=kiwi)           :: desired
      INTEGER                       :: loc, lb, ub
      INTEGER,INTENT(IN),optional   :: sindex
100   FORMAT("CopyValues:  uncached dvar passed:",a,a)
      desired = c
      desired = uc(desired)
      if (PRESENT(sindex)) then
    	lb = sindex - 5
    	if (lb < 1) lb = 1
    	ub = sindex + 5
    	if (lb > size(array)) lb = size(array)
      	do loc=lb,ub
        	if (array(loc)==desired) return
	end do
      end if
      do loc=1,UBOUND(array,1)
         if (array(loc)==desired) return
      end do
      WRITE(msg,100) CHAR(13),TRIM(desired)
      call stopWithError
    end function findit

  end subroutine copyValues


  ! sets the cache start date equal to the current date, offset by deltaTS timesteps
  ! deltaTS typically would be negative
  subroutine changeCacheDate(cache, deltaTS)
    TYPE(VariableCache), INTENT(INOUT):: cache
    INTEGER, INTENT(IN) :: deltaTS
    cache%cache_date = offsetDate(simulation%current_date, deltaTS)  ! the date corresponding to the first item in cache
    cache%current_offset = -deltaTS + 1  ! position in the cache of the current date
    IF(debug) WRITE( message_unit, *) 'changeCacheDate:  offset is now', cache%current_offset
  end subroutine changeCacheDate


  ! changes the simulation date by the given number of timesteps
  ! deltTS typically would be +1
  subroutine changeSimDate(deltaTS)
    INTEGER,INTENT(IN) :: deltaTS
    simulation%current_date = offsetDate(simulation%current_date, deltaTS)
    IF(debug) WRITE( message_unit, *) 'changeSimDate:  date is now ', simulation%current_date
  end subroutine changeSimDate


  !Returns a character date equal to the given date, offset by the supplied timesteps
  function offsetDate(old_date, deltaTS) RESULT(new_date)
    USE HEC_LIB_DSS
    CHARACTER(LEN=datelen), INTENT(IN) :: old_date
    INTEGER, INTENT(IN) :: deltaTS
    INTEGER :: i, juls,istime=1440,jule,ietime, intl, nvals
    CHARACTER(LEN=datelen)     :: new_date
    call datjul(old_date, juls, i)
    i=1
    call zgintl(intl,simulation%timestep,nvals,i)   ! retrieves intl in minutes
    IF (i<0) then
       msg='Bad timestep: ' // simulation%timestep
       call stopWithError
    end if
    i = inctim(intl, 0, deltaTS, juls, istime, jule, ietime)
    ietime=ietime
    new_date = ''
    call juldat(jule, 104, new_date, i)
  end function offsetDate


  ! Resets the log file.  Intended to be called at every read/flush
  subroutine resetLog
    IMPLICIT NONE
    REWIND(message_unit)
    WRITE(message_unit,*) 'Log reset.  Resuming at ' // simulation%current_date
  end subroutine resetLog


  ! Returns a DSS path name tailored for a given table
  function formPathname(vc,fpart) RESULT(pathname)
    USE HEC_LIB_DSS
    TYPE(SingleVariableCache), INTENT(IN) :: vc
    CHARACTER(LEN=80)                  :: pathname
    character(len=*), intent(in), optional ::  fpart
    INTEGER :: npath
    if ( present(fpart) ) then
       call zpath(simulation%apart, &                  ! a-part
            vc%NAME, &                           ! b-part
            vc%KIND, &                           ! c-part
            '', &                                ! d-part
            simulation%timestep, &               ! e-part
            fpart, &                           ! f-part
            pathname, npath)
    else
       call zpath(simulation%apart, &                  ! a-part
            vc%NAME, &                           ! b-part
            vc%KIND, &                           ! c-part
            '', &                                ! d-part
            simulation%timestep, &               ! e-part
            vc%DESC, &                           ! f-part
            pathname, npath)
    end if
    IF(npath<80) pathname(npath+1:) = ' '
    call verifypath(pathname)
  end function formPathname


  subroutine dssErrorMessage(istat)
    PRIVATE dssErrorMessage
    INTEGER :: istat
    CHARACTER(LEN=80) :: m
100 FORMAT("istat came back equalling",i3)
    WRITE(m,100) istat
    IF(istat==4)  m="Record found--but no data"
    IF(istat==5)  m="No records were found"
    IF(istat==11) m="Bad NVALS!"
    IF(istat==12) m="Bad time interval"
    IF(istat==15) m="Invalid date/time"
    IF(istat==24) m="Invalid pathname"
    msg = 'DSS says: ' // m
  end subroutine dssErrorMessage


  ! converts string to upper case, adjusts left, and removes trailing NUL's
  function uc(old) RESULT(new)
    CHARACTER(LEN=KIWI)             :: new
    CHARACTER(LEN=KIWI), INTENT(IN) :: old
    INTEGER :: i
    new=ADJUSTL(old)
    do i=1,LEN(old),1
       if (LGE(old(i:i),'a') .AND. LLE(old(i:i), 'z') ) then
          new(i:I)=ACHAR(IACHAR(old(i:i))-32)
       else
          IF(old(i:i) == CHAR(0)) new(i:i) = ' '
       end if
    END do
  end function uc


  ! fortran strings suck
  function makeKiwi(c) RESULT (c_kiwi)
    CHARACTER(LEN=*) :: c
    CHARACTER(LEN=kiwi) :: c_kiwi
    c_kiwi=''
    c_kiwi=c
  END function makeKiwi


  ! stops with error message printed
  subroutine stopWithError
    USE Winteracter
    implicit none
    PRIVATE stopWithError
    INTEGER i
100 FORMAT(/1x,'DSS ERROR:'/1x,a)
    call WInitialise(' ')
    call WMessageBox(0,3,1,msg,'WRIMS Wrangler')
    do i=1,LEN(msg)
       IF(msg(i:i) == CHAR(13)) msg(i+1:) = CHAR(10) // msg(i+1:)
    end do
    WRITE(message_unit,100) msg
    CLOSE(message_unit)
    call EXIT(1)    ! error level may be ignored in Windows
  end subroutine stopWithError

END module dss_cache
