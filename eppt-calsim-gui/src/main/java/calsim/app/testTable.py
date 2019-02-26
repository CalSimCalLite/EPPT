sys.add_package('calsim.app')
sys.add_package('calsim.gui')
from calsim.app import DerivedTimeSeries
from calsim.gui import DTSTable

dts = DerivedTimeSeries()
dts.setName("demo 1")
dts.setOperationIdAt(0,TimeSeriesMath.ADD)
dts.setBPartAt(0,"b-part")
dts.setCPartAt(0,"c-part")
dts.setOperationIdAt(1,TimeSeriesMath.SUB)
dts.setDTSNameAt(1,"demo2")
dts.getOperationIdAt(1)
table = DTSTable(dts)
fr = JFrame()
fr.getContentPane().add(table)
fr.pack()
fr.show()