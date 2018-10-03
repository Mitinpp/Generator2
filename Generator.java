import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;


public class Generator {


    public static void main(String[] args) throws IOException, Exception {

        String xml = "settings.xml";
        String tsv = "source-data.tsv";

        List<String> title = new ArrayList<>();  // Список имён столбцов
        List<Integer> width = new ArrayList<>(); // Список ширины столбцов
        int pageHeight = 0; // Высота страницы
        int pageWidth = 1; // Ширина страницы
        int sumOfWidth = 0; // Сумма значений в массиве со значениями Ширины столбцов
        int sumOfLines = 0; // Количество строк в будущей таблице
        int tsvLines = 0; // Количество строк в TSV
        int tsvColumns = 0; // Количество столбцов в будущей таблице
        String[][] pageArray; // Двумерный массив для данных будущей таблицы
        int o = 0;
        int p = 0;
        Integer[] pageLines;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  // Открытие файла settings.xml
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(xml));
        Element rootElement = document.getDocumentElement();


        NodeList n1 = rootElement.getElementsByTagName("page");     // Достаём высоту и ширину страницы
        if (n1.getLength() > 0) {
            Element el = (Element) n1.item(0);
            pageHeight = Integer.parseInt(el.getElementsByTagName("height").item(0).getTextContent());
            pageWidth = Integer.parseInt(el.getElementsByTagName("width").item(0).getTextContent());
        }

        NodeList n2 = rootElement.getElementsByTagName("column");   // Достаём список имён и значения ширины столбцов
        for (int i = 0; i < n2.getLength() ; i++) {
            Element el = (Element) n2.item(i);
            title.add(el.getElementsByTagName("title").item(0).getTextContent());
            width.add(Integer.parseInt(el.getElementsByTagName("width").item(0 ).getTextContent()));
            sumOfWidth += Integer.parseInt(el.getElementsByTagName("width").item(0 ).getTextContent());  // Заодно сразу считаем сумму щирины столбцов для последующей проверки
            tsvColumns += 1; // Считаем количество столбцов
        }


        tsvLines = countLinesNew(tsv);
        pageArray = new String[tsvLines][tsvColumns];

        pageArray = tsvRead(tsv, tsvLines, tsvColumns);

        for (int i = 0; i < tsvLines ; i++) {
            for (int j = 0; j < tsvColumns; j++) {
                System.out.print(pageArray[i][j] + "   ");
            }
            System.out.println();
        }

        System.out.println();

        int lines = 0;
        int max = 0;
        pageLines = new Integer[tsvLines];
        for (int i = 0; i < tsvLines ; i++) {
            for (int j = 0; j < tsvColumns; j++) {
                if (pageArray[i][j].length()/width.get(j) >= 1){
                    if (pageArray[i][j].length()/width.get(j) > max){
                        max = pageArray[i][j].length()/width.get(j);
                        if (pageArray[i][j].length()%width.get(j) == 0)
                            max -= 1;
                    }
                }
            }
            lines += max + 2;
            pageLines[i] = lines+1;
            max = 0;
        }
        for (int i = 0; i < tsvColumns ; i++) {
            if (title.get(i).length()/width.get(i) >=1 ) {
                if (title.get(i).length()/width.get(i) > max){
                    max = title.get(i).length()/width.get(i);
                    if (title.get(i).length()%width.get(i) == 0)
                        max -= 1;
                }

            }

        }
        lines += max + 1;

        System.out.println(lines);



    }


    public static int countLinesNew(String filename) throws IOException {    //Для реализации своего кода, я нашёл быстрый и лёгкий способ подсчета строк даже в очень больших файлах
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i=0; i<1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                for (int i=0; i<readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }

    public static String[][] tsvRead(String filename, int lines, int columns) throws IOException{
        String[][] pageArray = new String[lines][columns];
        File fileDir = new File(filename);  // Открываем файл TSV
        StringTokenizer st ;
        int i = 0;
        int j = 0;
        BufferedReader TSVFile = new BufferedReader(new InputStreamReader(
                new FileInputStream(fileDir), "UTF-16"));
        String dataRow = TSVFile.readLine();

        while (dataRow != null){    // Читаем значения из файла TSV
            st = new StringTokenizer(dataRow,"\t");

            while(st.hasMoreElements()){
                pageArray[i][j] = st.nextElement().toString();
                j++;
            }
            dataRow = TSVFile.readLine(); // Read next line of data.
            i++;
            j = 0;
        }
        TSVFile.close();
        return pageArray;
    }
    /*public static List<String> getString(String tagName, Element element) {
        List<String> stringList = new ArrayList<>();
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();

            if (subList != null && subList.getLength() > 0) {
                for (int i = 0; i < subList.getLength(); i++) {
                    stringList.add(subList.item(0).getNodeName());
                }
                return stringList;
            }
        }

        return null;
    }*/

    /*public static LinkedHashMap<String, Integer> GetSettings(String xml) throws Exception {

        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(xml));      // Открытие файла settings.xml
        //Document document = builder.parse(is);
        Element rootElement = document.getDocumentElement();
        NodeList n1 = rootElement.getElementsByTagName("page");
        if (n1.getLength() > 0) {
            Element el = (Element) n1.item(0);
            map.put(el.getElementsByTagName("height").item(0).getTextContent(),
                    Integer.parseInt(el.getElementsByTagName("width").item(0).getTextContent()));
        }

        NodeList n2 = rootElement.getElementsByTagName("column");
        for (int i = 0; i < n2.getLength() ; i++) {
            Element el = (Element) n2.item(i);
            map.put(el.getElementsByTagName("title").item(0).getTextContent(),
                    Integer.parseInt(el.getElementsByTagName("width").item(0 ).getTextContent()));
        }

        return map;
    }*/
    /*InputStream inputStream = new FileInputStream("settings.xml");
        Reader reader = new InputStreamReader(inputStream,"UTF-8");
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");*/



        /*NodeList n2 = nl.item(1).getChildNodes();
        for (int i = 0; i < n2.getLength() ; i++) {
            System.out.println(n2.item(i).getNodeName());
        }
        System.out.println();*/




        /*if (nl != null) {
            int length = nl.getLength();
            for (int i = 0; i < length; i++) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nl.item(i);
                    if (el.getNodeName().contains("column")) {
                        String title = el.getElementsByTagName("title").item(0).getTextContent();
                        String width = el.getElementsByTagName("width").item(0).getTextContent();
                        System.out.println(title);
                        System.out.println(width);
                    }
                }
            }
        }*/
    //inputStream.close();
}
