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

import java.io.File;
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

    private static File resultsFile;

    /**
     * Compiles team and equation data and writes it out to an excel workbook
     * @param location Location of results.xlsx
     */
    public static void write(String location){
        Main.debug("Creating workbook");
        //Create a new workbook
        wb = new XSSFWorkbook();
        Main.debug("Creating sheet");
        //Create a new sheet in the workbook with the name "Results"
        Writer.s = wb.createSheet("Results");

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
                //If location doesn't end in "/", add one
                resultsFile = new File((location.isEmpty()? location: location.charAt(location.length()-1)=='/'?location:location+"/") + "results.xlsx");
                if (resultsFile.getParentFile() != null) {
                    //noinspection ResultOfMethodCallIgnored
                    resultsFile.getParentFile().mkdirs();
                }
                //noinspection ResultOfMethodCallIgnored
                resultsFile.createNewFile();
                FileOutputStream fileOut = new FileOutputStream(resultsFile);
                //Write out workbook
                wb.write(fileOut);
                //Close output stream
                fileOut.close();
                Main.debug("Workbook saved");
                break;
            } catch (IOException e) {
                if (e.getMessage().contains("The process cannot access the file because it is being used by another process")){
                    Main.sendError("Close the Excel workbook! Press OK when done.",false);
                } else {
                    Main.sendError("Error in arguments passed for results.xlsx location",true);
                }
            }
        }
        Main.log("Results saved to " + resultsFile.getAbsolutePath());


    }

    /**
     * Takes data from the TEAM list and adds its data into the Excel workbook
     */
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
                Main.sendError(value + " is not a team number! (You should be proud of getting this error)",false);
            }

            //See above
            c = r.createCell(1);
            value = t.getValue(Team.MATCH_KEY);
            try {

                c.setCellValue(Integer.parseInt(value));
            } catch (NumberFormatException e1){
                Main.sendError(value + " is not a match number! (You should be proud of getting this error)",false);
            }

            //See above
            c = r.createCell(2);
            value = t.getValue(Team.COLOR_KEY);
            c.setCellValue(value);
            //Set color of text to be the same as alliance color
            if (value.equalsIgnoreCase("red")){
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
                            Main.sendError("Plist is missing key \"" + key + ",\" which is impressive.",false);
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

    /**
     * Sets the columns in the workbook to an appropriate width using the autosize feature
     *
     */
    private static void autoSize() {
        Main.debug("Autosizing Columns");
        int width;
        for (int i = 0; i<50;i++){
            //Autosize every column up to 50
            s.autoSizeColumn(i,true);
            //Get the new width of the column
            width = s.getColumnWidth(i);
            //Add enough to account for the "Filter" icon
            s.setColumnWidth(i,width+1200);
        }

        s.createFreezePane(0,2);
    }

    /**
     * Sets properties for the CellStyles used in other methods
     */
    private static void generateStyles() {
        Main.debug("Creating styles");
        Main.debug("Creating Header Style");

        //Create new CellStyle
        headerStyle = wb.createCellStyle();
        //Set borders
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setBorderTop(CellStyle.BORDER_THIN);
        headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
        headerStyle.setBorderRight(CellStyle.BORDER_THIN);
        //Align the cell to center
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);

        Main.debug("Creating Category Style");
        //Create new CellStyle
        categoryStyle = wb.createCellStyle();
        //Set borders
        categoryStyle.setBorderBottom(CellStyle.BORDER_THIN);
        categoryStyle.setBorderTop(CellStyle.BORDER_THIN);
        categoryStyle.setBorderLeft(CellStyle.BORDER_THIN);
        categoryStyle.setBorderRight(CellStyle.BORDER_THIN);
        //Align center
        categoryStyle.setAlignment(CellStyle.ALIGN_CENTER);
        //Set the font to Bold
        Font categoryFont = wb.createFont();
        categoryFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        categoryStyle.setFont(categoryFont);

        Main.debug("Creating Section End Style");
        //Create new CellStyle
        sectionEndStyle = wb.createCellStyle();
        //Set right border
        sectionEndStyle.setBorderRight(CellStyle.BORDER_THIN);

        Main.debug("Creating Red Style");
        //Create new CellStyle
        redStyle = wb.createCellStyle();
        //Set right border
        redStyle.setBorderRight(CellStyle.BORDER_THIN);
        //Set font color to Red
        Font redFont = wb.createFont();
        redFont.setColor(HSSFColor.RED.index);
        redStyle.setFont(redFont);

        Main.debug("Creating Blue Style");
        blueStyle = wb.createCellStyle();
        blueStyle.setBorderRight(CellStyle.BORDER_THIN);
        Font blueFont = wb.createFont();
        //Set Font color to blue
        blueFont.setColor(HSSFColor.BLUE.index);
        blueStyle.setFont(blueFont);

        Main.debug("Creating Super Secret Style");
        //OOH Shiny
        superSecretSpecialStyle = wb.createCellStyle();
        Font superSecretSpecialFont = wb.createFont();
        superSecretSpecialFont.setColor(HSSFColor.GOLD.index);
        superSecretSpecialFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        superSecretSpecialFont.setItalic(true);
        superSecretSpecialStyle.setFont(superSecretSpecialFont);
    }

    /**
     * Generates the top two rows of the Workbook, contains all titles
     */
    private static void createHeader() {
        Main.debug("Creating header");
        //Category Row
        Row topRow = s.createRow(0);
        //Title row
        Row bottomRow = s.createRow(1);

        //Generic Cell
        Cell c;

        //Add Team Number to first cell
        c=bottomRow.createCell(0);
        c.setCellValue("Team Number");
        c.setCellStyle(headerStyle);

        //Add Match Number to second cell
        c=bottomRow.createCell(1);
        c.setCellValue("Match Number");
        c.setCellStyle(headerStyle);

        //Add Alliance Color to third cell
        c=bottomRow.createCell(2);
        c.setCellValue("Alliance Color");
        c.setCellStyle(headerStyle);

        //Above the first three cells, put "General"
        c=topRow.createCell(0);
        c.setCellValue("General");
        c.setCellStyle(categoryStyle);

        //Merge the region above the first three cells
        s.addMergedRegion(new CellRangeAddress(0,0,0,2));

        //Keep track of where the section (i.e. Human, Teleop, Automomous) begins
        int sectionStart = 3;
        //Keep track of where the program is working
        int headerPosition = 3;

        //For every Element
        for (Element e: Main.getElements()){
            //Really only two options, Label or not label, this Switch Statement is kind of overkill
            switch(e.getType()){
                case LABEL:
                    //If one of the arguments is "distinguished" is is placed as a Category Header
                    if (e.getArguments()[0].trim().equalsIgnoreCase("distinguished")){
                        c = topRow.createCell(headerPosition);
                        //Place the label description in the top row
                        c.setCellValue(e.getDescriptions()[0]);
                        c.setCellStyle(categoryStyle);
                        //After the first category
                        if (headerPosition!=3){
                            //Merge everything since the last category
                            s.addMergedRegion(new CellRangeAddress(0,0,sectionStart,headerPosition-1));
                            //List of where the sections end so that borders can be added
                            endColumns.add(headerPosition-1);
                        }
                        //The section starts at the current position
                        sectionStart = headerPosition;
                    }
                    break;
                default:
                    //For every key
                    for (String key:e.getKeys()){
                        c = bottomRow.createCell(headerPosition);

                        //Capitalize the first letter of every word
                        String[] splitKey = key.split("_");
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i<splitKey.length;i++){
                            builder.append(capitalize(splitKey[i])).append(" ");
                        }
                        //Put the String in the new cell
                        c.setCellValue(builder.toString().trim());
                        c.setCellStyle(headerStyle);

                        //Move to next cell
                        headerPosition++;
                    }
                    break;
            }

        }
        //Merge the top row since the last category, placed manually here because there are no more labels to trigger it
        s.addMergedRegion(new CellRangeAddress(0,0,sectionStart,headerPosition-1));
        endColumns.add(headerPosition-1);

        //New section
        sectionStart = headerPosition;

        //Totals
        c = topRow.createCell(headerPosition);
        c.setCellValue("Totals");
        c.setCellStyle(headerStyle);

        //For every Equation
        for(Equation e: Main.getEquations()){
            c = bottomRow.createCell(headerPosition);
            //Set equation name as column title
            c.setCellValue(e.getName());
            c.setCellStyle(headerStyle);
            headerPosition++;
        }

        //Add "Grand Total" to final column
        c = bottomRow.createCell(headerPosition);
        c.setCellValue("Grand Total");
        c.setCellStyle(headerStyle);

        //Merge the "Totals" category header
        s.addMergedRegion(new CellRangeAddress(0,0,sectionStart,headerPosition));
        endColumns.add(headerPosition);

        //Add filter to bottom row
        s.setAutoFilter(new CellRangeAddress(1,1,0,headerPosition));

    }


    /**
     * Capitalize the first letter of a word
     */
    private static String capitalize(String input){
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Simple getter for results.xlsx
     *
     * @return File object for results.xlsx
     */
    public static File getResultsFile() {
        return resultsFile;
    }
}
