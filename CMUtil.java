/*
 * Decompiled with CFR 0_116.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.BooleanUtils
 *  org.apache.commons.lang.StringUtils
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.misys.tiplus2.configuration.management.tool;

import com.misys.tiplus2.configuration.management.tool.ConfigurationManagementTool;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManagementTool.class);

    public static boolean isObsolete(Set<String> argColumns, ResultSet argResultSet) throws SQLException {
        boolean obsolete = false;
        if (argColumns.contains("OBSOLETE")) {
            obsolete = BooleanUtils.toBoolean((String)argResultSet.getString("OBSOLETE"));
        } else if (argColumns.contains("NOT_OBSOLETE")) {
            obsolete = !BooleanUtils.toBoolean((String)argResultSet.getString("NOT_OBSOLETE"));
        }
        return obsolete;
    }

    public static String getMaintenanceType(Set<String> argColumns, ResultSet argResultSet) throws SQLException {
        String rtn = "F";
        if (CMUtil.isObsolete(argColumns, argResultSet)) {
            rtn = "D";
        }
        return rtn;
    }

    static void setUpDatesCal(String argBusinessDays, String argStandardDays, Integer argYear, Map<String, Object> mapping) {
        int i;
        if (argYear == null || argBusinessDays == null) {
            return;
        }
        ArrayList specialWorkingDays = new ArrayList();
        ArrayList specialNonWorkingDays = new ArrayList();
        GregorianCalendar cal = new GregorianCalendar(argYear, 0, 1);
        int firstDayOfTheYear = cal.get(7);
        if (cal.getFirstDayOfWeek() != 2) {
            firstDayOfTheYear = firstDayOfTheYear == 1 ? (firstDayOfTheYear += 6) : --firstDayOfTheYear;
        }
        int daysInYear = cal.isLeapYear(argYear) ? 366 : 365;
        String w54 = "";
        String defaultBusinessDays = "";
        for (i = 0; i < 54; ++i) {
            w54 = w54.concat(argStandardDays);
        }
        defaultBusinessDays = w54.substring(firstDayOfTheYear - 1, daysInYear + firstDayOfTheYear + 1);
        for (i = 0; i <= daysInYear - 1; ++i) {
            LinkedHashMap<String, String> specialWorkingDay = new LinkedHashMap<String, String>();
            LinkedHashMap<String, String> specialNonWorkingDay = new LinkedHashMap<String, String>();
            String currentValueBoolString = String.valueOf(argBusinessDays.charAt(i));
            if (currentValueBoolString.equalsIgnoreCase(String.valueOf(defaultBusinessDays.charAt(i)))) continue;
            cal.set(6, i + 1);
            String dateNew = CMUtil.formatToTIDate(cal.getTime().toString(), "");
            if ("Y".equalsIgnoreCase(currentValueBoolString)) {
                specialWorkingDay.put("SpecialWorkingDay", dateNew);
                specialWorkingDays.add(specialWorkingDay);
                continue;
            }
            specialNonWorkingDay.put("SpecialNonWorkingDay", dateNew);
            specialNonWorkingDays.add(specialNonWorkingDay);
        }
        mapping.put("SpecialNonWorkingDays", specialNonWorkingDays);
        mapping.put("SpecialWorkingDays", specialWorkingDays);
    }

//    static String formatToTIDate(String date, String dateFormat) {
//        if (date != null && !date.trim().isEmpty()) {
//            if (dateFormat == null || dateFormat.trim().isEmpty()) {
//                dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
//            }
//            String pattern = "yyyy-MM-dd";
//            Date TIDate = null;
//            try {
//                SimpleDateFormat df = new SimpleDateFormat(dateFormat);
//                TIDate = df.parse(date);
//            }
//            catch (ParseException ex) {
//                LOG.error("Problem parsing date.", (Throwable)ex);
//            }
//            SimpleDateFormat TIDateFormat = new SimpleDateFormat(pattern);
//            date = TIDateFormat.format(TIDate);
//        }
//        return date;
//    }
    static String formatToTIDate(String datein, String dateFormat) {
        String date = ""; //I added this

//        LOG.info("<< Date >>" + date);
//        LOG.info("<< DateFormat >>" + dateFormat);
        if ((datein != null) && (!datein.trim().isEmpty())) {

            //I added this
            if (!datein.contains(":")) {
                date = datein.concat(" 00:00:00");
            }
            //Ends here

            if ((dateFormat == null) || (dateFormat.trim().isEmpty())) {
                dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
            }

            String pattern = "yyyy-MM-dd";
            Date TIDate = null;
            try {
                SimpleDateFormat df = new SimpleDateFormat(dateFormat);
                TIDate = df.parse(date);
            } catch (ParseException ex) {
                LOG.error("Problem parsing date.", ex);
            }
            SimpleDateFormat TIDateFormat = new SimpleDateFormat(pattern);
            date = TIDateFormat.format(TIDate);
        }
        return date;
    }

    static String formatToTIDate2(String date) {
        if (date != null && !date.trim().isEmpty()) {
            if (date.length() > 6) {
                date = date.substring(1, 7);
            }
            date = CMUtil.formatToTIDate(date, "yyMMdd");
        }
        return date;
    }

    static String formatToAmountDecimal(String amountStr, String ccyEditCode) {
        int ccyDecimalPlaces;
        if (StringUtils.isNotBlank((String)amountStr) && StringUtils.isNotBlank((String)ccyEditCode) && (ccyDecimalPlaces = Integer.parseInt(ccyEditCode)) > 0) {
            amountStr = StringUtils.mid((String)amountStr, (int)0, (int)(amountStr.length() - ccyDecimalPlaces)) + "." + StringUtils.right((String)amountStr, (int)ccyDecimalPlaces);
        }
        return amountStr;
    }

    public static String formatToInsertDecimal(String argAmount) {
        String rtn = "";
        Integer amountLength = argAmount.length();
        if (amountLength > 0) {
            if (amountLength < 3) {
                argAmount = String.format("%03d", Integer.parseInt(argAmount));
                amountLength = argAmount.length();
            }
            StringBuilder original_Amount = new StringBuilder(argAmount);
            original_Amount.insert(amountLength - 2, ".");
            rtn = original_Amount.toString();
        }
        return rtn;
    }

    public static boolean hasCriteria(String operation) {
        String[] messagesWithCriteria = new String[]{"CustomerError", "EventChargeMap", "EventDocumentMap", "InterestAccountingMap", "InterestTypeMap", "PostingRule", "TeamEventMap", "TracerType"};
        boolean rtn = false;
        if (Arrays.asList(messagesWithCriteria).contains(operation)) {
            rtn = true;
        }
        return rtn;
    }

    public static <T> List<List<T>> getChunks(List<T> list, int chunkSize) {
        ArrayList<List<T>> sublists = new ArrayList<List<T>>();
        if (list.size() < chunkSize) {
            sublists.add(list);
        } else {
            int s = list.size();
            for (int i = 0; i < s; i += chunkSize) {
                int offset = i;
                int block = offset + chunkSize < s ? offset + chunkSize : s;
                sublists.add(list.subList(offset, block));
            }
        }
        return sublists;
    }

    public static List<File> getParentDirectories(File file) {
        ArrayList<File> parentDirectories = new ArrayList<File>();
        CMUtil.getParentDirectories(file, parentDirectories);
        return parentDirectories;
    }

    private static void getParentDirectories(File file, List<File> parentDirectories) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDirectories.contains(parentDir)) {
            parentDirectories.add(parentDir);
            CMUtil.getParentDirectories(parentDir, parentDirectories);
        }
    }

    public static List<String> getParentDirectoryPaths(File file, boolean nameOnly) {
        ArrayList<String> parentDirPaths = new ArrayList<String>();
        for (File dir : CMUtil.getParentDirectories(file)) {
            parentDirPaths.add(nameOnly ? dir.getName() : dir.getPath());
        }
        return parentDirPaths;
    }
}

