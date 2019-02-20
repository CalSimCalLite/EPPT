/*
 * Copyright (c) 2019
 * California Department of Water Resources
 * All Rights Reserved.  DWR PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval from DWR
 */

package calsim.msw;

import vista.set.DataReference;
import vista.set.DataSet;
import vista.set.DataSetAttr;
import vista.set.RegularTimeSeries;
import vista.time.TimeFactory;
import vista.time.TimeInterval;
import vista.time.TimeWindow;

public class LumpOperator extends ReferenceOperator
{

	protected LumpOperator(DataReference[] r)
	{
		super(r);
	}

	//Method required by ReferenceOperator
	protected DataSet[] returnDataSetArray(TimeWindow tw)
	{

		setTimeWindow(rIn, tw);

		DataSet[] ds = new DataSet[rIn.length];
		TimeFactory tf = TimeFactory.getInstance();
		for(int i = 0; i < rIn.length; i++)
		{
			DataSet tDS = rIn[i].getData();

			double lumpedValue = 0;
			for(int j = 0; j < tDS.size(); j++)
			{
				lumpedValue = lumpedValue + tDS.getElementAt(j).getY();
			}
			lumpedValue = lumpedValue / tDS.size();

			double[] Y = {lumpedValue};
			DataSetAttr tDSA = tDS.getAttributes();
			int[] flag = {tDS.getElementAt(0).getFlag()};
			ds[i] = new RegularTimeSeries(tDS.getName(),
					tw.getEndTime(),
					tf.createTimeInterval(1, TimeInterval.MONTH_INTERVAL),
					Y,
					flag,
					tDSA);
		}
		for(int i = 0; i < ds.length; i++)
		{
		}
		return ds;
	}
}
