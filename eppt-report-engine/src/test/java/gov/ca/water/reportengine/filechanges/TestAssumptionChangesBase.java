/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2019.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */

package gov.ca.water.reportengine.filechanges;

import gov.ca.water.reportengine.TestQAQCReportBase;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

class TestAssumptionChangesBase extends TestQAQCReportBase
{
    Element getInitialConditionsElem(Element elem)
    {
        NodeList elementsByTagName = elem.getElementsByTagName("initial-conditions");
        return (Element) elementsByTagName.item(0);
    }

    Element getStateVariablesElem(Element elem)
    {
        NodeList elementsByTagName = elem.getElementsByTagName("state-variables");
        return (Element) elementsByTagName.item(0);
    }

    String getCommonRecDifData(Element elem)
    {
        NodeList elementsByTagName = elem.getElementsByTagName("common-records-different-data");
        return ((Element) elementsByTagName.item(0)).getTextContent();
    }

    String getRecordsOnlyBase(Element elem)
    {
        NodeList elementsByTagName = elem.getElementsByTagName("records-only-in-base");
        return ((Element) elementsByTagName.item(0)).getTextContent();
    }

    String getRecordsOnlyAlt(Element elem)
    {
        NodeList elementsByTagName = elem.getElementsByTagName("records-only-in-alternative");
        return ((Element) elementsByTagName.item(0)).getTextContent();
    }

    List<String> getChangesList(Element elem)
    {
        NodeList elementsByTagName = elem.getElementsByTagName("records-list");
        NodeList records = ((Element) elementsByTagName.item(0)).getElementsByTagName("record");

        List<String> retVal = new ArrayList<>();
        for(int i = 0;i<records.getLength();i++)
        {
            retVal.add( records.item(i).getTextContent());
        }
        return retVal;
    }


}
