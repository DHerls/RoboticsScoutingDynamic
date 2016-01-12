package org.fullmetalfalcons.scouting.fileio;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
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
    private static CellStyle sectionEndStyle;
    private static CellStyle redStyle;
    private static CellStyle blueStyle;
    private static CellStyle superSecretSpecialStyle;

    private static ArrayList<Integer> endColumns = new ArrayList<>();

    public static void write() throws IOException {
        wb = new XSSFWorkbook();
        s = wb.createSheet("Results");

        generateStyles();
        createHeader();
        addData();
        autoSize();
        FileOutputStream fileOut = new FileOutputStream("results.xlsx");
        wb.write(fileOut);
        fileOut.close();
    }

    private static void addData() {
        Cell c;
        int rowNum = 2;
        Row r;
        for (Team t: Main.getTeams()){
            r = s.createRow(rowNum);

            c = r.createCell(0);
            Object o = t.getValue(Team.NUMBER_KEY);
            c.setCellValue(Integer.parseInt(o.toString()));
            if (Integer.parseInt(o.toString())==4557){
                c.setCellStyle(superSecretSpecialStyle);
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
        int width;
        for (int i = 0; i<50;i++){
            s.autoSizeColumn(i,true);
            width = s.getColumnWidth(i);
            s.setColumnWidth(i,width+1100);
        }

        s.createFreezePane(0,2);
    }

    private static void generateStyles() {
        headerStyle = wb.createCellStyle();
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setBorderTop(CellStyle.BORDER_THIN);
        headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
        headerStyle.setBorderRight(CellStyle.BORDER_THIN);
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);

        sectionEndStyle = wb.createCellStyle();
        sectionEndStyle.setBorderRight(CellStyle.BORDER_THIN);

        redStyle = wb.createCellStyle();
        redStyle.setBorderRight(CellStyle.BORDER_THIN);
        Font redFont = wb.createFont();
        redFont.setColor(HSSFColor.RED.index);
        redStyle.setFont(redFont);

        blueStyle = wb.createCellStyle();
        blueStyle.setBorderRight(CellStyle.BORDER_THIN);
        Font blueFont = wb.createFont();
        blueFont.setColor(HSSFColor.BLUE.index);
        blueStyle.setFont(blueFont);

        superSecretSpecialStyle = wb.createCellStyle();
        Font superSecretSpecialFont = wb.createFont();
        superSecretSpecialFont.setColor(HSSFColor.GOLD.index);
        superSecretSpecialFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        superSecretSpecialFont.setItalic(true);
        superSecretSpecialStyle.setFont(superSecretSpecialFont);
    }

    private static void createHeader() {
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
        c.setCellStyle(headerStyle);

        s.addMergedRegion(new CellRangeAddress(0,0,0,2));

        endColumns.add(3);

        int labelStart = 3;
        int headerPosition = 3;

        for (Element e: Main.getElements()){
            switch(e.getType()){
                case LABEL:
                    if (e.getArguments()[0].toLowerCase().trim().equals("distinguished")){
                        c = topRow.createCell(headerPosition);
                        c.setCellValue(e.getDescriptions()[0]);
                        c.setCellStyle(headerStyle);
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
                            builder.append(capitalize(splitKey[i]) + " ");
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
