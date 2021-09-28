package com.zw.platform.util.common;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;


public final class XmlUtil {
    private static Logger log = LogManager.getLogger(XmlUtil.class);

    private XmlUtil() {
        throw new Error("工具类不能实例化");
    }

    public static org.jdom2.Document buildFromFile(String filePath) {
        SAXBuilder builder = new SAXBuilder();
        org.jdom2.Document document = null;
        try {
            if (filePath.startsWith("http")) {
                log.info("filePath is  startWith htttp");
            }
            try {
                document = builder.build(new File(filePath));
            } catch (JDOMException e) {
                log.error(e.getMessage());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        label51: return document;
    }

    public static org.jdom2.Document buildFromXMLString(String xmlString) {
        SAXBuilder builder = new SAXBuilder();
        org.jdom2.Document document = null;
        try {
            try {
                document = builder.build(new StringReader(xmlString));
            } catch (JDOMException e) {

                log.error(e.getMessage());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return document;
    }

    public static org.jdom2.Document buildFromDom(org.w3c.dom.Document dom) {
        DOMBuilder builder = new DOMBuilder();
        org.jdom2.Document jdomDoc = builder.build(dom);
        return jdomDoc;
    }

    public static void outputToStdoutUTF8(org.jdom2.Document myDocument) {
        outputToStdout(myDocument, "UTF-8");
    }

    public static void outputToStdout(org.jdom2.Document myDocument, String encoding) {
        XMLOutputter outputter;
        try {
            outputter = new XMLOutputter();
            Format fm = Format.getPrettyFormat();
            fm.setEncoding(encoding);
            outputter.setFormat(fm);
            outputter.output(myDocument, System.out);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static String outputToString(org.jdom2.Document document) {
        return outputToString(document, "UTF-8");
    }

    public static String outputToString(org.jdom2.Document document, String encoding) {
        ByteArrayOutputStream byteRep = new ByteArrayOutputStream();
        XMLOutputter outputter = new XMLOutputter();
        Format fm = Format.getPrettyFormat();
        fm.setEncoding(encoding);
        outputter.setFormat(fm);
        try {
            outputter.output(document, byteRep);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return byteRep.toString();
    }

    public static String outputToString(List list) {
        return outputToString(list, "UTF-8");
    }

    public static String outputToString(List list, String encoding) {
        ByteArrayOutputStream byteRep = new ByteArrayOutputStream();
        XMLOutputter outputter = new XMLOutputter();
        Format fm = Format.getPrettyFormat();
        fm.setEncoding(encoding);
        outputter.setFormat(fm);
        try {
            outputter.output(list, byteRep);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return byteRep.toString();
    }

    public static org.w3c.dom.Document outputToDom(org.jdom2.Document jdomDoc) throws JDOMException {
        DOMOutputter outputter = new DOMOutputter();
        return outputter.output(jdomDoc);
    }

    public static void outputToFile(org.jdom2.Document myDocument, String filePath) {
        outputToFile(myDocument, filePath, "UTF-8");
    }

    public static void outputToFile(org.jdom2.Document myDocument, String filePath, String encoding) {
        XMLOutputter outputter;
        try {
            outputter = new XMLOutputter();
            Format fm = Format.getPrettyFormat();
            fm.setEncoding(encoding);
            outputter.setFormat(fm);

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), encoding);
            outputter.output(myDocument, writer);
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static void executeXSL(org.jdom2.Document myDocument, String xslFilePath, StreamResult xmlResult) {
        TransformerFactory tfactory = TransformerFactory.newInstance();

        DOMOutputter outputter = new DOMOutputter();
        try {
            org.w3c.dom.Document domDocument = null;
            try {
                domDocument = outputter.output(myDocument);
            } catch (JDOMException e) {
                log.error(e.getMessage());
            }
            Source xmlSource = new DOMSource(domDocument);
            StreamSource xsltSource = null;
            try {
                xsltSource = new StreamSource(new FileInputStream(xslFilePath));
            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
            }

            Transformer transformer = tfactory.newTransformer(xsltSource);

            transformer.transform(xmlSource, xmlResult);
        } catch (javax.xml.transform.TransformerException e) {
            log.error(e.getMessage());
        }
    }
}