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

package gov.ca.water.reportengine.executivereport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestModelInputsRow
{

//    private static Logger LOGGER = Logger.getLogger(TestModelInputsRow.class.getName());
//    private Document _qAQCMaster;
//    private Document _qAQCReportToTest;
//
//
//
//
//    private ExecutiveReportHeader createERHeader()
//    {
//        int baseViolation = 5;
//        int altViolation = 6;
//        String pctChangeViolations = "7% increase";
//        double avgAnnualExportPrev = 8.0;
//        double avgAnnualExportCur = 9.0;
//        String annualExportIncDec = "increase";
//        double avgCvpNodStoragePrev = 11;
//        double avgCvpNodStorageCur = 12;
//        String cvpNodStorIncDec = "increase";
//        double avgSWPNodStorPrev = 14;
//        double avgSWPNodStorCur = 15;
//        String swpNodStorIncDec = "increase";
//
//        ExecutiveReportHeader erHeader = new ExecutiveReportHeader(baseViolation, altViolation, pctChangeViolations, avgAnnualExportPrev, avgAnnualExportCur, annualExportIncDec,
//                avgCvpNodStoragePrev, avgCvpNodStorageCur, cvpNodStorIncDec, avgSWPNodStorPrev, avgSWPNodStorCur, swpNodStorIncDec);
//        return erHeader;
//    }
//
//    private void loadQAQCReportMaster() throws Exception
//    {
//        URL resource = this.getClass().getClassLoader().getResource("QAQCReport.xml");
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//        _qAQCMaster = dBuilder.parse(resource.getPath());
//       _qAQCMaster.getDocumentElement().normalize();
//    }
//
//    private void loadReportToTest(String path) throws Exception
//    {
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//        _qAQCReportToTest = dBuilder.parse(path);
//        _qAQCReportToTest.getDocumentElement().normalize();
//    }
//
//
//
//
//
//    @Test
//    void testWritingAndReadingModelInputsRowElementWithNoAlternative() throws Exception
//    {
//        String path = System.getProperty("user.dir");
//        String filePath = path + "\\executivereport.xml";
//
//        //create ER
//        List<ModelInputs> mis = new ArrayList<>();
//        ExecutiveReport er = new ExecutiveReport(createERHeader(), mis, new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),
//                new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>());
//
//        //createXMLElement out the file
//        ExecutiveReportXMLCreator erWriter = new ExecutiveReportXMLCreator();
//        erWriter.createXMLElement(filePath, er);
//
//
//        //read the xml file to test
//        loadReportToTest(filePath);
//        loadQAQCReportMaster();
//
//        List<Element> modelInputElems = ExecutiveReportTestUtilities.getModuleElementsWithName(_qAQCReportToTest, ExecutiveReportXMLCreator.MI);
//        List<Element> masterModelInputElems = ExecutiveReportTestUtilities.getModuleElementsWithName(_qAQCMaster, ExecutiveReportXMLCreator.MI);
//
//        //check that the number of modules (# of columns) is equal to the base + number of alternatives
//        Assertions.assertTrue(modelInputElems.size() == 1);
//
//        for(int i = 0;i<modelInputElems.size();i++)
//        {
//            Assertions.assertEquals(ExecutiveReportTestUtilities.getStudyOrderForModule(masterModelInputElems.get(i)), ExecutiveReportTestUtilities.getStudyOrderForModule(modelInputElems.get(i)));
//            Assertions.assertEquals(ExecutiveReportTestUtilities.getModelEntriesOrderForModule(masterModelInputElems.get(i)), ExecutiveReportTestUtilities.getModelEntriesOrderForModule(modelInputElems.get(i)));
//
//            Assertions.assertEquals(ExecutiveReportTestUtilities.getStudyValue(masterModelInputElems.get(i)), ExecutiveReportTestUtilities.getStudyValue(modelInputElems.get(i)));
//            Assertions.assertEquals(ExecutiveReportTestUtilities.getModelEntriesValue(masterModelInputElems.get(i)),ExecutiveReportTestUtilities.getModelEntriesValue(modelInputElems.get(i)));
//        }
//
//    }
//
//    @Test
//    void testWritingAndReadingModelInputsRowElementWith1Alternative() throws Exception
//    {
//        String path = System.getProperty("user.dir");
//        String filePath = path + "\\executivereport.xml";
//
//        //input values
//        int tSUpdatedInSV = 31;
//        int changed = 21;
//        int added = 1;
//        int deleted = 2;
//
//        //create ER
//        List<ModelInputs> mis = new ArrayList<>();
//        mis.add(new ModelInputs(tSUpdatedInSV,changed,added,deleted));
//        ExecutiveReport er = new ExecutiveReport(createERHeader(), mis, new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),
//                new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),new ArrayList<>());
//
//        //createXMLElement out the file
//        ExecutiveReportXMLCreator erWriter = new ExecutiveReportXMLCreator();
//        erWriter.createXMLElement(filePath, er);
//
//
//        //read the xml file to test
//        loadReportToTest(filePath);
//        loadQAQCReportMaster();
//
//        List<Element> modelInputElems = ExecutiveReportTestUtilities.getModuleElementsWithName(_qAQCReportToTest, ExecutiveReportXMLCreator.MI);
//        List<Element> masterModelInputElems = ExecutiveReportTestUtilities.getModuleElementsWithName(_qAQCMaster, ExecutiveReportXMLCreator.MI);
//
//        //check that the number of modules (# of columns) is equal to the base + number of alternatives
//        Assertions.assertTrue(modelInputElems.size() == 2);
//        Assertions.assertTrue(modelInputElems.size() == masterModelInputElems.size());
//
//
//        for(int i = 0;i<modelInputElems.size();i++)
//        {
//            Assertions.assertEquals(ExecutiveReportTestUtilities.getStudyOrderForModule(masterModelInputElems.get(i)), ExecutiveReportTestUtilities.getStudyOrderForModule(modelInputElems.get(i)));
//            Assertions.assertEquals(ExecutiveReportTestUtilities.getModelEntriesOrderForModule(masterModelInputElems.get(i)), ExecutiveReportTestUtilities.getModelEntriesOrderForModule(modelInputElems.get(i)));
//
//            Assertions.assertEquals(ExecutiveReportTestUtilities.getStudyValue(masterModelInputElems.get(i)), ExecutiveReportTestUtilities.getStudyValue(modelInputElems.get(i)));
//            Assertions.assertEquals(ExecutiveReportTestUtilities.getModelEntriesValue(masterModelInputElems.get(i)),ExecutiveReportTestUtilities.getModelEntriesValue(modelInputElems.get(i)));
//        }
//    }



}