import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Дмитрий on 20.03.14.
 */
public class TableExplorer {
    private final int columnMax = 2000;
    private final int rowMax = 100;
    private boolean ignoreHeaders = true;
    File[] tableFiles;
    File outDirectory;
    String outName;
    String regexp;
    String separators;
    String[] headers = new String[]{"Название таблицы",
            "Найденные поля (%)", "Оставшиеся поля (%)",
            "Среднее по столбцам", "Обратное среднее по столбцам",
            "Среднее по строкам", "Обратное среднее по строкам",
            "Полностью соответствующие столбцы", "Полностью не соответствующие столбцы",
            "Полностью соответствующие строки", "Полностью не соответствующие строки",
            "Всего строк", "Всего столбцов", "Всего полей"
    };
    Matcher matcher;
    Pattern pattern;
    //for all tables
    int fullColumnPercentAll = 0;
    int fullRowPercentAll = 0;
    int avgColAll = 0;
    int avgRowAll = 0;
    double avgColCountAll = 0;
    double avgRowCountAll = 0;
    double avgAllFieldCountAll = 0;
    int percentAll = 0;

    //for current table
    int allFieldCount = 0;
    int matchesCount = 0;
    int fullColumns = 0;
    int emptyColumns = 0;
    int fullRows = 0;
    int emptyRows = 0;
    int matchesInRow = 0;
    double avgCol = 0;
    double avgRow = 0;
    int columns = 0;
    int rows = 0;
    int fullColumnPercent = 0;
    int emptyColumnPercent = 0;
    int fullRowPercent = 0;
    int emptyRowPercent = 0;
    int[] colFinds;
    int[] rowFinds;
    //ArrayList<Integer> colFinds = new ArrayList<Integer>(columnMax);
    //ArrayList<Integer> rowFinds = new ArrayList<Integer>(rowMax);

    int percent = 0;

    private int emptyColumnPercentAll = 0;
    private int emptyRowPercentAll = 0;
    private String encoding = "windows-1251";
    private int length;

    public TableExplorer(File[] tableFiles, File outDirectory, String outName, String regexp, String separators) {
        this.tableFiles = tableFiles;
        this.outDirectory = outDirectory;
        this.outName = outName;
        this.regexp = regexp;
        separators = separators.replaceAll("[\\\\][t]", "\t");
        Config config = Config.getInstance();
        ignoreHeaders = config.isIgnoreHeaders();
        encoding = config.getEncoding();
        this.separators = separators;
        pattern = Pattern.compile(regexp);
        length = tableFiles.length;
    }

    public void createOutTable() {
        outDirectory.mkdirs();
        File outFile = new File(outDirectory.getPath() + "/" + outName + ".csv");
        try {
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outFile.getPath()), encoding), separators.charAt(0));
            setHeaders(writer);
            for (File f : tableFiles) {
                InputStreamReader inputStream1 = new InputStreamReader(new FileInputStream(f.getPath()), encoding);
                InputStreamReader inputStream2 = new InputStreamReader(new FileInputStream(f.getPath()), encoding);
                char separator = detect(inputStream1, separators);
                CSVReader reader = new CSVReader(inputStream2, separator);
                addData(writer, reader, f.getName());
                reader.close();
            }
            writeFinalAvg(writer);
            writer.close();
        } catch (IOException e) {

        }
    }

    private void writeFinalAvg(CSVWriter writer) throws IOException {
        String[] resRecord = new String[]{"По всем таблицам (в среднем)",
                percentAll / length + "", (100 - percentAll / length) + "",
                avgColAll / length + "", (100 - avgColAll / length) + "",
                avgRowAll / length + "", (100 - avgRowAll / length) + "",
                fullColumnPercentAll / length + "", emptyColumnPercentAll / length + "",
                fullRowPercentAll / length + "", emptyRowPercentAll / length + "",
                ((int) avgColCountAll) + "", ((int) avgRowCountAll) + "", ((int) avgAllFieldCountAll) + ""
        };
        writer.writeNext(resRecord);
    }

    private void setHeaders(CSVWriter writer) throws IOException {
        writer.writeNext(headers);
    }


    public static char detect(InputStreamReader reader, String separators) throws IOException {
        int[] separatorsCount = new int[separators.length()];
        int character = reader.read();
        if (character == -1) return '\0';
        int nextCharacter = 0;
        boolean quoted = false;
        boolean firstChar = true;

        do {
            nextCharacter = reader.read();
            switch (character) {
                case '"':
                    if (quoted) {
                        if (nextCharacter != '"') // Value is quoted and
                            // current character is " and next character is not ".
                            quoted = false;
                        /*else
                            reader.Read(); // Value is quoted and current and
                        // next characters are "" - read (skip) peeked qoute.*/
                    } else {
                        if (firstChar)    // Set value as quoted only if this quote is the
                            // first char in the value.
                            quoted = true;
                    }
                    break;
                case '\n':
                    if (!quoted) {
                        firstChar = true;
                        continue;
                    }
                    break;
                default:
                    if (!quoted) {
                        int index = separators.indexOf(character);
                        if (index != -1) {
                            ++separatorsCount[index];
                            firstChar = true;
                            continue;
                        }
                    }
                    break;
            }

            if (firstChar)
                firstChar = false;
            character = nextCharacter;
        } while (nextCharacter != -1);

        int maxCount = separatorsCount[0];
        int maxId = 0;
        for (int i = 1; i < separators.length(); i++) {
            if (separatorsCount[i] > maxCount) {
                maxCount = separatorsCount[i];
                maxId = i;
            }
        }
        reader.close();
        return maxCount == 0 ? '\0' : separators.charAt(maxId);
    }


    private void addData(CSVWriter writer, CSVReader reader, String name) throws IOException {

        List<String[]> records = reader.readAll();
        if (ignoreHeaders) {
            records.remove(0);
        }
        rows = records.size();
        columns = records.get(0).length;
        allFieldCount = rows * columns;
        initCurTableVariables();
        for (int i = 0; i < rows; i++) {
            matchesInRow = 0;
            String[] record = records.get(i);
            analyzingRecord(record);
            rowFinds[i] = matchesInRow;
        }
        countingForCurTable();
        String[] result = generateResultRecord(name);
        writer.writeNext(result);
    }

    private String[] generateResultRecord(String name) {
        return new String[]{name,
                percent + "", (100 - percent) + "",
                (int) avgCol + "", (100 - (int) avgCol) + "",
                (int) avgRow + "", (100 - (int) avgRow) + "",
                fullColumnPercent + "", emptyColumnPercent + "",
                fullRowPercent + "", emptyRowPercent + "",
                columns + "", rows + "", allFieldCount + ""
        };
    }

    private void analyzingRecord(String[] values) {
        for (int i = 0; i < columns; i++) {
            matcher = pattern.matcher(values[i]);
            if (matcher.matches()) {
                colFinds[i]++;
                matchesCount++;
                matchesInRow++;
            }
        }
    }

    private void countingForCurTable() {
        allFieldCount = rows * columns;
        countingByColumns();
        countingByRows();
        avgAllFieldCountAll += ((double) allFieldCount) / length;
        percent = (int) ((100.0 * matchesCount) / allFieldCount);
        percentAll += percent;
    }

    private void countingByColumns() {
        for (int i = 0; i < columns; i++) {
            int curCol = colFinds[i];
            if (curCol == rows) fullColumns++;
            if (curCol == 0) emptyColumns++;
            double curAvgCol = ((double) curCol) / rows;
            avgCol += curAvgCol;
        }
        avgCol = 100 * avgCol / columns;
        fullColumnPercent = (int) ((100.0 * fullColumns) / columns);
        fullColumnPercentAll += fullColumnPercent;
        emptyColumnPercent = (int) ((100.0 * emptyColumns) / columns);
        emptyColumnPercentAll += emptyColumnPercent;
        avgColAll += avgCol;
        avgColCountAll += ((double) columns) / length;

    }

    private void countingByRows() {
        for (int i = 0; i < rows; i++) {
            int curRow = rowFinds[i];
            if (curRow == columns) fullRows++;
            if (curRow == 0) emptyRows++;
            avgRow += ((double) curRow) / columns;
        }

        avgRow = 100 * avgRow / rows;
        avgRowAll += avgRow;
        fullRowPercent = (int) ((100.0 * fullRows) / rows);
        fullRowPercentAll += fullRowPercent;
        emptyRowPercent = (int) ((100.0 * emptyRows) / rows);
        emptyRowPercentAll += emptyRowPercent;
        avgRowCountAll += ((double) rows) / length;
    }

    private void initCurTableVariables() {
        colFinds = new int[columns];
        rowFinds = new int[rows];
        matchesCount = 0;
        fullColumns = 0;
        emptyColumns = 0;
        fullRows = 0;
        emptyRows = 0;
        matchesInRow = 0;
        avgCol = 0;
        avgRow = 0;
    }

}
