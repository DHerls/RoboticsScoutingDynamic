package org.fullmetalfalcons.scouting.fileio;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.elements.ElementType;
import org.fullmetalfalcons.scouting.equations.Equation;
import org.fullmetalfalcons.scouting.main.Main;
import org.fullmetalfalcons.scouting.teams.Team;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Writes out to an Excel document
 *
 * Created by Dan on 1/12/2016.
 */
public class Writer {

    private static Workbook wb;
    private static Sheet s;

    private static CellStyle headerStyle;
    private static CellStyle categoryStyle;
    private static CellStyle sectionEndStyle;
    private static CellStyle redStyle;
    private static CellStyle blueStyle;
    private static CellStyle superSecretSpecialStyle;

    private static final ArrayList<Integer> endColumns = new ArrayList<>();

    public static final String FILENAME = "results.xlsx";

    /**
     * Only public method, compiles data and writes it out to an excel workbook
     */
    public static void write(){
        Main.debug("Creating workbook");
        //Create a new workbook
        wb = new XSSFWorkbook();
        Main.debug("Creating sheet");
        //Create a new sheet in the workbook with the name "Results"
        s = wb.createSheet("Results");

        //Create CellStyles for use later
        generateStyles();

        //Generate the first two rows of the sheet form the available Elements
        createHeader();

        //Make the columns an appropriate width
        autoSize();

        //Add Team data to the sheet
        addData();

        Main.debug("Attempting to save workbook");

        //Will only end if successful in saving the file, should only fail if workbook is open in another window
        while (true) {
            try {
                //Create file if none exists/open existing file
                FileOutputStream fileOut = new FileOutputStream(FILENAME);
                //Write out workbook
                wb.write(fileOut);
                //Close output stream
                fileOut.close();
                Main.debug("Workbook saved");
                break;
            } catch (IOException e) {
                Main.sendError("Close the Excel workbook! Press OK when done.");
            }
        }
        Main.log("Results saved to " + Writer.FILENAME);


    }

    private static void addData() {
        Main.debug("Adding data");
        //Create generic cell to use for things
        Cell c;
        //Start after the header
        int rowNum = 2;
        Row r;
        //For all team data in TEAMS
        for (Team t: Main.getTeams()){
            Main.debug("Adding data for team " + t.getValue(Team.NUMBER_KEY));
            //Create a row
            r = s.createRow(rowNum);

            //These first few sections are for keys which are constants: Team Number, Match Number, Alliance Color
            c = r.createCell(0);
            //Get value associated with key
            String value = t.getValue(Team.NUMBER_KEY);
            try {
                //Assume Team Number is an int, catch the error if it isn't
                c.setCellValue(Integer.parseInt(value));
                //Shhhhhhhhhhhhh
                if (value.hashCode()==1601763) {
                    c.setCellStyle(superSecretSpecialStyle);
                }
            } catch (NumberFormatException e1){
                Main.sendError(value + " is not a team number! (You should be proud of getting this error)");
            }

            //See above
            c = r.createCell(1);
            value = t.getValue(Team.MATCH_KEY);
            try {

                c.setCellValue(Integer.parseInt(value));
            } catch (NumberFormatException e1){
                Main.sendError(value + " is not a match number! (You should be proud of getting this error)");
            }

            //See above
            c = r.createCell(2);
            value = t.getValue(Team.COLOR_KEY);
            c.setCellValue(value);
            //Set color of text to be the same as alliance color
            if (value.toLowerCase().equals("red")){
                c.setCellStyle(redStyle);
            } else {
                c.setCellStyle(blueStyle);
            }

            //Start in the column after the General Info
            int columnNum = 3;
            //For every element
            for (Element e: Main.getElements()){
                //Labels don't have values, so ignore them
                if (e.getType()!= ElementType.LABEL){
                    //For every key in the element
                    for (String key: e.getKeys()){
                        //Create a new cell
                        c = r.createCell(columnNum);

                        try{
                            //Assume value is an integer
                            c.setCellValue(Integer.parseInt(t.getValue(key)));
                        } catch (NumberFormatException e1){
                            try {
                                //If it fails, assume value is a double
                                c.setCellValue(Double.parseDouble(t.getValue(key)));
                            } catch (NumberFormatException e2){
                                //Finally, default to String
                                c.setCellValue(t.getValue(key));
                            }
                        } catch (NullPointerException e1){
                            Main.sendError("Plist is missing key \"" + key + ",\" which is impressive.");
                            c.setCellValue("MISSING VALUE");
                        }
                        //If at the end of a section, add a border on the right
                        if (endColumns.contains(columnNum)){
                            c.setCellStyle(sectionEndStyle);
                        }

                        columnNum++;
                    }
                }
            }

            //Section to calculate score totals

            double grandTotal = 0.0;

            //Used to temporarily store value
            double v;
            //For every equation
            for (Equation e: Main.getEquations()){
                //Create a new cell
                c = r.createCell(columnNum);
                //Calculate the score for the current team
                v = e.evaluate(t);
                //Set the cell to equal the score
                c.setCellValue(v);
                //Add the score to the Grand Total
                grandTotal+=v;
                columnNum++;
            }

            //Place value of Grand Total in final cell
            c = r.createCell(columnNum);
            c.setCellValue(grandTotal);
            //Add a border on the right side
            c.setCellStyle(sectionEndStyle);

            rowNum++;
        }
    }

    private static void autoSize() {
        Main.debug("Autosizing Columns");
        int width;
        for (int i = 0; i<50;i++){
           s.autoSizeColumn(i,true);
            width = s.getColumnWidth(i);
            s.setColumnWidth(i,width+1200);
        }

        s.createFreezePane(0,2);
    }

    private static void generateStyles() {
        Main.debug("Creating styles");
        Main.debug("Creating Header Style");
        headerStyle = wb.createCellStyle();
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setBorderTop(CellStyle.BORDER_THIN);
        headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
        headerStyle.setBorderRight(CellStyle.BORDER_THIN);
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);

        Main.debug("Creating Category Style");
        categoryStyle = wb.createCellStyle();
        categoryStyle.setBorderBottom(CellStyle.BORDER_THIN);
        categoryStyle.setBorderTop(CellStyle.BORDER_THIN);
        categoryStyle.setBorderLeft(CellStyle.BORDER_THIN);
        categoryStyle.setBorderRight(CellStyle.BORDER_THIN);
        categoryStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font categoryFont = wb.createFont();
        categoryFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        categoryStyle.setFont(categoryFont);

        Main.debug("Creating Section End Style");
        sectionEndStyle = wb.createCellStyle();
        sectionEndStyle.setBorderRight(CellStyle.BORDER_THIN);

        Main.debug("Creating Red Style");
        redStyle = wb.createCellStyle();
        redStyle.setBorderRight(CellStyle.BORDER_THIN);
        Font redFont = wb.createFont();
        redFont.setColor(HSSFColor.RED.index);
        redStyle.setFont(redFont);

        Main.debug("Creating Blue Style");
        blueStyle = wb.createCellStyle();
        blueStyle.setBorderRight(CellStyle.BORDER_THIN);
        Font blueFont = wb.createFont();
        blueFont.setColor(HSSFColor.BLUE.index);
        blueStyle.setFont(blueFont);

        Main.debug("Creating Super Secret Style");
        superSecretSpecialStyle = wb.createCellStyle();
        Font superSecretSpecialFont = wb.createFont();
        superSecretSpecialFont.setColor(HSSFColor.GOLD.index);
        superSecretSpecialFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        superSecretSpecialFont.setItalic(true);
        superSecretSpecialStyle.setFont(superSecretSpecialFont);
    }

    private static void createHeader() {
        Main.debug("Creating header");
        Row topRow = s.createRow(0);
        Row bottomRow = s.createRow(1);

        Cell c;

        c=bottomRow.createCell(0);
        c.setCellValue("Team Number");
        c.setCellStyle(headerStyle);

        c=bottomRow.createCell(1);
        c.setCellValue("Match Number");
        c.setCellStyle(headerStyle);

        c=bottomRow.createCell(2);
        c.setCellValue("Alliance Color");
        c.setCellStyle(headerStyle);

        c=topRow.createCell(0);
        c.setCellValue("General");
        c.setCellStyle(categoryStyle);

        s.addMergedRegion(new CellRangeAddress(0,0,0,2));

        int labelStart = 3;
        int headerPosition = 3;

        for (Element e: Main.getElements()){
            switch(e.getType()){
                case LABEL:
                    if (e.getArguments()[0].toLowerCase().trim().equals("distinguished")){
                        c = topRow.createCell(headerPosition);
                        c.setCellValue(e.getDescriptions()[0]);
                        c.setCellStyle(categoryStyle);
                        if (headerPosition!=3){
                            s.addMergedRegion(new CellRangeAddress(0,0,labelStart,headerPosition-1));
                            endColumns.add(headerPosition-1);
                        }
                        labelStart = headerPosition;
                    }
                    break;
                default:
                    for (String key:e.getKeys()){
                        c = bottomRow.createCell(headerPosition);
                        String[] splitKey = key.split("_");
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i<splitKey.length;i++){
                            builder.append(capitalize(splitKey[i])).append(" ");
                        }
                        c.setCellValue(builder.toString().trim());
                        c.setCellStyle(headerStyle);

                        headerPosition++;
                    }
                    break;
            }

        }
        s.addMergedRegion(new CellRangeAddress(0,0,labelStart,headerPosition-1));
        endColumns.add(headerPosition-1);

        labelStart = headerPosition;

        c = topRow.createCell(headerPosition);
        c.setCellValue("Totals");
        c.setCellStyle(headerStyle);

        for(Equation e: Main.getEquations()){
            c = bottomRow.createCell(headerPosition);
            c.setCellValue(e.getName());
            c.setCellStyle(headerStyle);
            headerPosition++;
        }

        c = bottomRow.createCell(headerPosition);
        c.setCellValue("Grand Total");
        c.setCellStyle(headerStyle);

        s.addMergedRegion(new CellRangeAddress(0,0,labelStart,headerPosition));
        endColumns.add(headerPosition);

        s.setAutoFilter(new CellRangeAddress(1,1,0,headerPosition));

    }


    private static String capitalize(String input){
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
