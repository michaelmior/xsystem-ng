package xsystem.learning;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xsystem.XStructure;

public class LearningModel {

    private final static Logger LOG = LoggerFactory.getLogger(LearningModel.class.getName());

    public final static String defaultXLabelRef = "src/main/resources/Learned";

    public void learnStructs(String inputFolder, String outputJsonFile) {

        File folder = new File(inputFolder);
        File outFile = new File(outputJsonFile);

        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            List<XStructType> result = new ArrayList<>();

            if(folder.isDirectory()){
                for (File file : folder.listFiles()) {
                    LOG.info("Started Learning From File " + file.getName());
                    
                    CSVParser parser = new CSVParserBuilder()
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build();

                    CSVReader reader = new CSVReaderBuilder(new FileReader(file.getPath()))
                    .withCSVParser(parser)
                    .build();

                    List<String[]> myEntries = reader.readAll();
                    
                    ArrayList<ArrayList<String>> columns = new ArrayList<>();

                    for(String s : myEntries.get(0)){
                        ArrayList<String> column = new ArrayList<>();
                        column.add(s);
                        columns.add(column);
                    }
                    
                    for(int i=0; i<myEntries.size(); i++){
                        if(i==0);

                        else{
                            String[] row = myEntries.get(i);
                            if(row.length != columns.size())
                                LOG.info("Error in CSV file");

                            for(int j=0; j<row.length ; j++){
                                if(row[j] == null)
                                    columns.get(j).add("");
                                else
                                    columns.get(j).add(row[j]);
                            }
                        }
                    }
                    
                    for(ArrayList<String> col : columns){
                        XStructure x = new XStructure();

                        String colName = col.get(0);
                        col.remove(0);

                        XStructure learned = x.addNewLines(col);
                        XStructType xandType = new XStructType(colName, learned);

                        LOG.info(colName + " has XStruct " + learned);
                        result.add(xandType);
                    }
                }
            }
            else{
                LOG.info("Started Learning From File " + folder.getName());
                    
                CSVParser parser = new CSVParserBuilder()
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build();

                CSVReader reader = new CSVReaderBuilder(new FileReader(folder.getPath()))
                .withCSVParser(parser)
                .build();

                List<String[]> myEntries = reader.readAll();
                    
                ArrayList<ArrayList<String>> columns = new ArrayList<>();

                for(String s : myEntries.get(0)){
                    ArrayList<String> column = new ArrayList<>();
                    column.add(s);
                    columns.add(column);
                }
                    
                for(int i=0; i<myEntries.size(); i++){
                    if(i==0);

                    else{
                        String[] row = myEntries.get(i);
                        if(row.length != columns.size())
                            LOG.info("Error in CSV file");

                        for(int j=0; j<row.length ; j++){
                            if(row[j] == null)
                                columns.get(j).add("");
                            else
                                columns.get(j).add(row[j]);
                        }
                    }
                }
                    
                for(ArrayList<String> col : columns){
                    XStructure x = new XStructure();

                    String colName = col.get(0);
                    col.remove(0);

                    XStructure learned = x.addNewLines(col);
                    XStructType xandType = new XStructType(colName, learned);

                    LOG.info(colName + " has XStruct " + learned);
                    result.add(xandType);
                }
            }
            writer.writeValue(outFile, result);
        }

        catch (Exception e) {
            LOG.info("[Error] " + e);
        }
    }

    public String labelAssignment(ArrayList<String> strList, String referenceFilePath){
        File folder = new File(referenceFilePath);

        ObjectMapper mapper = new ObjectMapper();
        String result = "Not Known";
        double score = 0.0;
        XStructure toBeAssigned = new XStructure().addNewLines(strList);

        try {
            if(folder.isDirectory()){
                for (File file : folder.listFiles()) {
                    ArrayList<Double> scoreLst = new ArrayList<>();
                    List<XStructType> structs = mapper.readValue(file, new TypeReference<List<XStructType>>(){});

                    for(XStructType struct : structs){
                        double particularScore = struct.xStructure.subsetScore(toBeAssigned);
                        scoreLst.add(particularScore);
                    }

                    double maxVal = Collections.max(scoreLst);
                    int maxIdx = scoreLst.indexOf(maxVal);

                    if(maxVal > score){
                        score = maxVal;
                        result = structs.get(maxIdx).type;
                    }
                    LOG.info("After referring from File - " + file.getName() + " computed type is " + result);
                }
            }
            else{
                ArrayList<Double> scoreLst = new ArrayList<>();
                List<XStructType> structs = mapper.readValue(folder, new TypeReference<List<XStructType>>(){});

                for(XStructType struct : structs){
                    double particularScore = struct.xStructure.subsetScore(toBeAssigned);
                    scoreLst.add(particularScore);
                }

                double maxVal = Collections.max(scoreLst);
                int maxIdx = scoreLst.indexOf(maxVal);

                if(maxVal > score){
                    score = maxVal;
                    result = structs.get(maxIdx).type;
                }
                LOG.info("After referring from File - " + folder.getName() + " computed type is " + result);
            }
        } catch (Exception e) {
            LOG.info("[Error] " + e);
        }

        return result;
    }

    public void labelAssignmentWithRegex(String sampleRegexFolderPath, String learnedXStructsJSONfolderpath, String outFile){
        File sampleRegexFolder = new File(sampleRegexFolderPath);
        ArrayList<Pair<String, String>> regexList = new ArrayList<>();

        if(sampleRegexFolder.isDirectory()){
            for(File file : sampleRegexFolder.listFiles()){
                try {
                    CSVParser parser = new CSVParserBuilder()
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build();

                    CSVReader reader = new CSVReaderBuilder(new FileReader(file.getPath()))
                    .withCSVParser(parser)
                    .build();

                    List<String[]> myEntries = reader.readAll();

                    for(String[] entry : myEntries){
                        String regex = entry[1];
                        String type = entry[0];
                        try {
                            Pattern.compile(regex);
                            regexList.add(Pair.with(regex, type));
                        } catch (Exception exception) {
                            LOG.info("[Error] PatternSyntaxException in " + regex + " of type " + type + " in file " + file.getName());
                            continue;
                        }
                    }

                } catch (Exception e) {
                    LOG.info("[Error] " + e);
                }
            }
        }

        else{
            try {
                CSVParser parser = new CSVParserBuilder()
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build();

                CSVReader reader = new CSVReaderBuilder(new FileReader(sampleRegexFolder.getPath()))
                .withCSVParser(parser)
                .build();

                List<String[]> myEntries = reader.readAll();

                for(String[] entry : myEntries){
                    String regex = entry[1];
                    String type = entry[0];
                    try {
                        Pattern.compile(regex);
                        regexList.add(Pair.with(regex, type));
                    } catch (Exception exception) {
                        LOG.info("[Error] PatternSyntaxException in " + regex + " of type " + type + " in file " + sampleRegexFolder.getName());
                        continue;
                    }
                }

            } catch (Exception e) {
                LOG.info("[Error] " + e);
            }
        }

        File folder = new File(learnedXStructsJSONfolderpath);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        ArrayList<XStructType> result = new ArrayList<>();
        try {
            if(folder.isDirectory()){
                for(File file : folder.listFiles()){
                    List<XStructType> structs = mapper.readValue(file, new TypeReference<List<XStructType>>(){});
                    for(XStructType struct : structs){
                        String randomString = struct.xStructure.generateStrings(1).get(0).replace("$", "");
                        String type = struct.type;
                        for(Pair<String, String> regandType : regexList){
                            if(Pattern.matches(regandType.getValue0(), randomString)){
                                type = regandType.getValue1();
                                break;
                            }
                        }
                        LOG.info("XStruct of colName " + struct.type + " is labeled " + type);

                        result.add(new XStructType(type, struct.xStructure));
                    }
                }
            }

            else{
                List<XStructType> structs = mapper.readValue(folder, new TypeReference<List<XStructType>>(){});
                for(XStructType struct : structs){
                    String randomString = struct.xStructure.generateStrings(1).get(0).replace("$", "");
                    String type = struct.type;
                    for(Pair<String, String> regandType : regexList){
                        if(Pattern.matches(regandType.getValue0(), randomString)){
                            type = regandType.getValue1();
                            break;
                        }
                    }
                    LOG.info("XStruct of colName " + struct.type + " is labeled " + type);

                    result.add(new XStructType(type, struct.xStructure));
                }
            }
            
            writer.writeValue(new File(outFile), result);

        } catch (Exception e) {
            LOG.info("[Error] " + e);
        }
    }

    public ArrayList<XStructType> readXStructsfromJSON(String path) {
        File folder = new File(path);
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<XStructType> result = new ArrayList<>();
        try {
            if(folder.isDirectory()){
                for(File file : folder.listFiles()){
                    LOG.info("Started reading from file - " + file.getName());
                    List<XStructType> structs = mapper.readValue(file, new TypeReference<List<XStructType>>(){});
                    result.addAll(structs);
                }
            }
            else{
                LOG.info("Started reading from file - " + folder.getName());
                List<XStructType> structs = mapper.readValue(folder, new TypeReference<List<XStructType>>(){});
                result.addAll(structs);
            }
        } catch (Exception e) {
            LOG.info("[Error] " + e);
        }
        return result;
    }
}