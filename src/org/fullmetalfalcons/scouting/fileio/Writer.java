package org.fullmetalfalcons.scouting.fileio;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fullmetalfalcons.scouting.elements.Element;
import org.fullmetalfalcons.scouting.elements.ElementType;
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

    private static ArrayList<Integer> endColumns = new ArrayList<>();

    public static final String FILENAME = "results.xlsx";

    public static void write(){
        Main.debug("Creating workbook");
        wb = new XSSFWorkbook();
        Main.debug("Creating sheet");
        s = wb.createSheet("Results");

        generateStyles();
        createHeader();
        addData();
        autoSize();
        Main.debug("Attempting to save workbook");

        while (true) {
            try {
                FileOutputStream fileOut = new FileOutputStream(FILENAME);
                wb.write(fileOut);
                fileOut.close();
                Main.debug("Workbook saved");
                break;
            } catch (IOException e) {
                Main.sendError("Close the Excel workbook! Press OK when done.");
            }
        }


    }

    private static void addData() {
        Main.debug("Adding data");
        Cell c;
        int rowNum = 2;
        Row r;
        for (Team t: Main.getTeams()){
            Main.debug("Adding data for team " + t.getValue(Team.NUMBER_KEY).toString());
            r = s.createRow(rowNum);

            c = r.createCell(0);
            Object o = t.getValue(Team.NUMBER_KEY);
            try {
                c.setCellValue(Integer.parseInt(o.toString()));
                if (Integer.parseInt(o.toString()) == 4557) {
                    c.setCellStyle(superSecretSpecialStyle);
                }
            } catch (NumberFormatException e1){
                Main.sendError(o.toString() + " is not a team number! (You should be proud of getting this error)");
            }

            c = r.createCell(1);
            o = t.getValue(Team.MATCH_KEY);
            c.setCellValue(Integer.parseInt(o.toString()));

            c = r.createCell(2);
            o = t.getValue(Team.COLOR_KEY);
            c.setCellValue(o.toString());
            if (o.toString().toLowerCase().equals("red")){
                c.setCellStyle(redStyle);
            } else {
                c.setCellStyle(blueStyle);
            }


            int columnNum = 3;
            for (Element e: Main.getElements()){
                if (e.getType()!= ElementType.LABEL){
                    for (String key: e.getKeys()){
                        c = r.createCell(columnNum);
                        try{
                            c.setCellValue(Integer.parseInt(t.getValue(key).toString()));
                        } catch (NumberFormatException e1){
                            try {
                                c.setCellValue(Double.parseDouble(t.getValue(key).toString()));
                            } catch (NumberFormatException e2){
                                c.setCellValue(t.getValue(key).toString());
                            }
                        }

                        if (endColumns.contains(columnNum)){
                            c.setCellStyle(sectionEndStyle);
                        }

                        columnNum++;
                    }
                }
            }
            rowNum++;
        }
    }

    private static void autoSize() {
        Main.debug("Autosizing Columns");
        int width;
        for (int i = 0; i<50;i++){
            s.autoSizeColumn(i,true);
            width = s.getColumnWidth(i);
            s.setColumnWidth(i,width+1100);
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
        s.setAutoFilter(new CellRangeAddress(1,1,0,headerPosition-1));

    }


    private static String capitalize(String input){
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
