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

!     Interface (API) to HEC-DSS functionality for WRIMS
!     D. McFadden (March, 1998)

!----------------------- Wresl Functions -------------------------------------------

! Function to retrieve input data from a DSS time-series database (in Wresl)
function dssin(pos, deltaT)
  USE DSSIO
  dll_export DSSIN
  REAL                         :: dssin
  INTEGER, INTENT(IN)          :: pos, deltaT
  dssin = getCachedValue(parmTable, pos, deltaT)
end function dssin

! Function to retrieve previous values of decision variables (in Wresl)
function dssdvar(pos, deltaT)
  USE DSSIO
  dll_export DSSDVAR
  REAL                        :: dssdvar
  INTEGER, INTENT(IN)          :: pos, deltaT
  dssdvar = getCachedValue(dvarTable, pos, deltaT)
end function dssdvar

! Function to retrieve previous CYCLE values of decision variables (in Wresl)
function dsscycle(pos, deltaCycle)
  USE DSSIO
  dll_export DSSCYCLE
  REAL                         :: dsscycle
  INTEGER, INTENT(IN)          :: pos, deltaCycle
  dsscycle = getCachedValue(dvarCycleTable, pos, deltaCycle) ! NEW ITEM ****
end function dsscycle

!----------------------- Called by DSS_INIT (via Wresl) -----------------------------

! Subroutine to inform us of the size of the cache tables
! THIS MUST BE CALLED FIRST!
subroutine dss_init_tables(dvarSize, parmSize)
  USE DSSIO
  dll_export DSS_INIT_TABLES
  INTEGER, INTENT(IN) :: dvarSize, parmSize
  ! WRITE (*,*) 'Before ALLOCATE(dvarTable%tab(dvarSize), parmTable%tab(parmSize))'  !DEBUGGING
  ALLOCATE(dvarTable%tab(dvarSize), parmTable%tab(parmSize))
  ! WRITE (*,*) 'After ALLOCATE(dvarTable%tab(dvarSize), parmTable%tab(parmSize))'  !DEBUGGING
  ALLOCATE(dvarHeaderTable(dvarSize))
  ! WRITE (*,*) 'After ALLOCATE(dvarHeaderTable(dvarSize))'  !DEBUGGING
  ALLOCATE(dvarCycleTable%tab(dvarSize)) ! NEW ITEM *****
  ! WRITE (*,*) 'After ALLOCATE(dvarCycleTable%tab(dvarSize))'  !DEBUGGING
  dvarTable%isDvarCache = .true.
  dvarCycleTable%isDvarCache = .true. ! NEW ITEM *****
  parmTable%isDvarCache = .false.
  dvarTable%tab%name=''
  dvarCycleTable%tab%name=''          ! NEW ITEM *****
  parmTable%tab%name=''
  dvarHeaderTable = ''
end subroutine dss_init_tables

! Subroutine to initialize storage for a decision variable
! THIS IS CALLED ONCE FOR EVERY DECISION VARIABLE THAT IS DEFINED
subroutine dss_dvar_init(pos, var_name, var_kind, var_units, var_desc, var_header)
  USE DSSIO
  dll_export DSS_DVAR_INIT
  CHARACTER(LEN=*), INTENT(IN) :: var_name, var_kind, var_units, var_desc, var_header
  OPTIONAL :: var_desc, var_header
  INTEGER, INTENT(IN) :: pos

  if (present(var_desc)) then
     call addVariable(pos, dvarTable%tab,  &
          makeKiwi(var_name), makeKiwi(var_kind),makeKiwi(var_units), &
          vdesc = makeKiwi(var_desc))

     call addVariable(pos, dvarCycleTable%tab, &
          makeKiwi(var_name), makeKiwi(var_kind),makeKiwi(var_units), &
          vdesc = makeKiwi(var_desc)) ! NEW ITEM ******

  else
     call addVariable(pos, dvarTable%tab,  &
          makeKiwi(var_name), makeKiwi(var_kind),makeKiwi(var_units))

     call addVariable(pos, dvarCycleTable%tab,  & ! NEW ITEM *****
          makeKiwi(var_name), makeKiwi(var_kind),makeKiwi(var_units))
  end if
  if (PRESENT(var_header)) then
     dvarHeaderTable(pos) = var_header
     call removeColons(dvarHeaderTable(pos))
  end if
end subroutine dss_dvar_init

! Subroutine to initialize storage for a time-series parameter variable
! THIS IS CALLED ONCE FOR EVERY TIMESERIES DEFINITION THAT IS DEFINED
subroutine dss_ts_init(pos, var_name, var_kind, var_units, var_desc)
  USE DSSIO
  dll_export DSS_TS_INIT
  CHARACTER(LEN=*), INTENT(IN) :: var_name, var_kind, var_units, var_desc
  OPTIONAL :: var_desc
  INTEGER,INTENT(IN) :: pos
  if (present(var_desc)) then
     call addVariable(pos, parmTable%tab,  &
          makeKiwi(var_name), makeKiwi(var_kind),makeKiwi(var_units), &
          vdesc = makeKiwi(var_desc))
  else
     call addVariable(pos, parmTable%tab,  &
          makeKiwi(var_name), makeKiwi(var_kind),makeKiwi(var_units))
  end if
end subroutine dss_ts_init
! set the f part for the init file
subroutine dss_set_init_fpart(fpart)
  use dssio
  dll_export dss_set_init_fpart
  character(len=*), intent(in) :: fpart
  init_fpart = fpart
end subroutine dss_set_init_fpart

!---------------------- Subs used by Wrapper -----------------------------------------

! Initialize this module, and open files
! dvar_f is the DSS filename to use to store/retrieve decision variables
! parm_f is the DSS filename to use to retrieve parameter (timeseries) data
! bpath the the base pathname (the a,d,e, and f parts) to use.  Other parts of the path
!   name must be null.  For example:  /CALSIM-V122///01OCT1997/1MONTH/STUDY/
! The simulation will start at the date specified as the D-part of this pathname, and
! the model time step will be what you said in the E-part.
subroutine dss_open(ivar_f,dvar_f, parm_f, bpath, debugging_wanted)
  USE DSSIO
  dll_export DSS_OPEN
  CHARACTER(LEN=*),INTENT(IN)    :: ivar_f,dvar_f, parm_f
  CHARACTER(LEN=*),INTENT(IN)    :: bpath   ! the a,e, and f parts of the DSS pathname
  LOGICAL, INTENT(IN)            :: debugging_wanted

  call initsimulation(bpath)
  IF (debugging_wanted) call setDebug
  if (chareq(ivar_f,dvar_f)) then
  	init_sameas_dvar = .true.
  	first_call = .false.
  end if
  if (.not. init_sameas_dvar) call zzopen(ivar_f,initvar_ifltab)
  call zzopen(dvar_f,dvar_ifltab)
  call zzopen(parm_f,parm_ifltab)
  return
END subroutine dss_open


! This routine deletes any exsiting solution data in the decision variable DSS file
! Its parameter is the number of time periods to clear, generally all the way to the end of the
! current simulation period, and generally beyond the current end of cache.
subroutine dss_clear_dvars( start_date, nper)
  USE dssio
  dll_export DSS_CLEAR_DVARS
  INTEGER, INTENT(IN) :: nper
  CHARACTER(LEN=datelen) :: start_date
  call clearValues( dvar_ifltab, dvarTable, dvarHeaderTable, start_date, nper)
end subroutine dss_clear_dvars

! Write out all buffered data to disk, then close files (all done).
subroutine dss_close
  USE dssio
  dll_export DSS_CLOSE
  if (per_offset > 0) then
  	call flushOut(dvar_ifltab, dvarTable, dvarHeaderTable, per_offset)
  else
  	call flushOut(dvar_ifltab, dvarTable, dvarHeaderTable)
  end if
  DEALLOCATE(dvarHeaderTable)
  call killOff(dvarTable)
  call killOff(parmTable)
  if (.not. init_sameas_dvar) call zzclose(initvar_ifltab)
  call zzclose(dvar_ifltab)
  call zzclose(parm_ifltab)
end subroutine dss_close

! Informs DSS as to how many periods of
!   lookahead to read in to cache (for the ts parameter data), including the current one,
!   and how many periods to read in to cache for previous values of dvars,
!   excluding the current one.
! Subroutine will write out any new period values of decision varibles that have been sent
!   since the previous call to this subroutine or since dss_open was called.
! Example:  call dss_read_flush(1, 12)                !  after a year.  Uses internal date.
subroutine dss_read_flush(nper_back, nper_fore, keep_log)
  USE DSSIO
  dll_export DSS_READ_FLUSH
  INTEGER, INTENT(in) :: nper_back,nper_fore
  LOGICAL, INTENT(IN) :: keep_log

  if (.not. keep_log) call resetLog

  per_offset = nper_back
  ! First, write out changes in the cached data
	if (first_call) then
  	call flushOut (dvar_ifltab, dvarTable, dvarHeaderTable, per_offset)
	else
  	call flushOut (dvar_ifltab, dvarTable, dvarHeaderTable)
    per_offset = 0
  end if

  ! Next, make new decision variable cache
  call allocateStorage (dvarTable, nper_fore+nper_back+1) ! make dvar cache same size - don't really need nper_for
  call changeCacheDate (dvarTable, -nper_back)
  if ( first_call ) then
 !    WRITE (*,*) 'calling readIn for the initvars'  !DEBUGGING
     call readIn(initvar_ifltab, dvarTable, .true., init_fpart)
     first_call = .false.
  else
 !    WRITE (*,*) 'calling readIn for the dvars'  !DEBUGGING
     call readIn (dvar_ifltab, dvarTable, .true.)
  end if

  ! Next, make new parameter variable cache
  call allocateStorage (parmTable, nper_fore+nper_back+1) ! add one for current TS
  call changeCacheDate (parmTable, -nper_back)
  call readIn (parm_ifltab, parmTable, .false.)
end subroutine dss_read_flush


! Initializes the CYCLE cache. Allocates storage and reads in initial data.
! Calls to this subroutine are usuall cycle_read_flush(0,number_of_cycles,logical)
subroutine cycle_read_flush(nper_back, nper_fore, keep_log)
  USE DSSIO
  dll_export CYCLE_READ_FLUSH
  INTEGER, INTENT(in) :: nper_back,nper_fore
  LOGICAL, INTENT(IN) :: keep_log

  if (.not. keep_log) call resetLog

  ! Next, make new decision variable cache
  call allocateStorage (dvarCycleTable, nper_fore+nper_back+1)
  call changeCacheDate (dvarCycleTable, -nper_back)
end subroutine cycle_read_flush


! Tells DSS to increment the simulation date.  Uses the E-part of the pathname
! specified in the DSS_OPEN call to figure out how much to increment.
subroutine dss_inc_date
  USE dssio
  dll_export DSS_INC_DATE
  call changeSimDate(1)
  call incrementCache(dvarTable)
  call incrementCache(parmTable)
end subroutine dss_inc_date

! Tells dvarCycleTable to increment by one cycle.  Uses date as increment but
! cache access only looks back in relative terms.
subroutine dss_inc_cycle
  USE dssio
  dll_export DSS_INC_CYCLE
  call incrementCache(dvarCycleTable)
end subroutine dss_inc_cycle


! Writes out data for one decision variable, for the current date.
! Doesn't actually write to disk; that is done by dss_read_flush.
! This just takes the LP solution and adds it to the cache.
subroutine dss_save_dvar(dvar_id, dvar_values, cacheType)
  USE DSSIO
  dll_export DSS_SAVE_DVAR
  REAL(8),                    INTENT(IN)  :: dvar_values(:)
  CHARACTER(LEN=DVAR_LEN), INTENT(IN)  :: dvar_id(:)
  INTEGER, INTENT(IN)             :: cacheType  !1=dvarTable 2=dvarCycleTable
  call SetflushRequired
  if (cacheType==1) call copyvalues(dvarTable,   dvar_values, dvar_id)
  if (cacheType==2) call copyvalues(dvarCycleTable,   dvar_values, dvar_id) ! NEW ITEM ****
end subroutine dss_save_dvar


! Utility subroutines for user header processing
subroutine removeColons(c)
  CHARACTER(LEN=*), INTENT(INOUT) :: c
  do j=1,LEN_TRIM(c)
     if (c(j:j)==';' .or. c(j:j)==':') c(j:j)=' '
  end do
end subroutine removeColons
