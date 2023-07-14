import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Currency {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        /* https://www.cbr.ru/scripts/XML_daily.asp?date_req=dd/MM/yyyy
         *
         * https://services.nbrb.by/xmlexrates.aspx?ondate=dd/MM/yyyy
         * */

        String[][] rates = getRates();

        JFrame frame = new JFrame();
        frame.setTitle("Курс валют");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] columnName = {"Код валюты", "Цена", "Название валюты"};
        JTable table = new JTable(rates, columnName);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 20));

        table.setFont(new Font("Serif", Font.PLAIN, 18));
        table.setRowHeight(table.getRowHeight() + 16);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);


        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static String[][] getRates() throws ParserConfigurationException, IOException, SAXException {
        String[][] rates;
        HashMap<String, NodeList> result = new HashMap<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        String url = "https://services.nbrb.by/xmlexrates.aspx?ondate=" + dateFormat.format(date);
        Document doc = loadDocument(url);

        NodeList nl = doc.getElementsByTagName("Currency");

        for (int i = 0; i < nl.getLength(); i++) {
            Node c = nl.item(i);
            NodeList nlChild = c.getChildNodes();
            for (int j = 0; j < nlChild.getLength(); j++) {
                if (nlChild.item(j).getNodeName().equals("CharCode")) {
                    result.put(nlChild.item(j).getTextContent(), nlChild);
                }
            }
        }

        int k = 0;
        rates = new String[result.size()][3];

        for (Map.Entry<String, NodeList> entry: result.entrySet()) {
            NodeList temp = entry.getValue();
            double value = 0;
            int nominal = 0;
            String nameCurrent = "";
            for (int i =0; i < temp.getLength(); i++) {
                if (temp.item(i).getNodeName().equals("Rate")) {
                    value = Double.parseDouble(temp.item(i).getTextContent().replace(",","."));
                } else if (temp.item(i).getNodeName().equals("Scale")) {
                    nominal = Integer.parseInt(temp.item(i).getTextContent());
                } else if (temp.item(i).getNodeName().equals("Name")) {
                    nameCurrent = temp.item(i).getTextContent();
                }
            }
            double amount = value / nominal;
            rates[k][0] = "1 " + entry.getKey();
            rates[k][1] = (((double) Math.round(amount * 10000)) / 10000) + " BLR";
            rates[k][2] = nameCurrent;
            k++;
        }
        return rates;
    }

    private static Document loadDocument(String url) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder().parse(new URL(url).openStream());
    }
}
