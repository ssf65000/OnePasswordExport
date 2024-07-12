package com.ssf.onepasswordexport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssf.onepasswordexport.types.OnePasswordItem;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class OPExport {
    static ObjectMapper xs_objectMapper = new ObjectMapper();
    private static List<String> xs_tagToExport = new ArrayList<>();
    private static String xs_vaultId = null;
    private static String xs_outFile = "op.csv";

    public static boolean xs_truncateUrl = false;

    public static void main(String []args) throws Exception {

        log("OnePassword export. ProcessId: "+ ManagementFactory.getRuntimeMXBean().getName() );
        parseCli(args);
        Set<String> ids = readAllIds();
        log("Number of ids to read : " + ids.size());
        List<OnePasswordItem> records = new ArrayList<>();
        for(String itemId : ids){
            readItems(itemId, records);
        }
        records.sort((o1, o2) -> {
            int ret1 = o1.getCsvOutputCategory().compareToIgnoreCase(o2.getCsvOutputCategory());
            if (ret1 != 0)
                return ret1;
            return o1.getName().compareToIgnoreCase(o2.getName());
        });
        File f = new File(xs_outFile);
        if(f.exists()) {
            f.delete();
        }
        CsvWriter csvWriter = null;
        String previousCategory = null;
        for(OnePasswordItem opi : records){
            if(!opi.getCsvOutputCategory().equalsIgnoreCase(previousCategory)){
                if(csvWriter != null)
                    csvWriter.close();
                csvWriter = openCsv(opi, f, records);
                previousCategory = opi.getCsvOutputCategory();
            }
            csvWriter.writeRow(opi.getCsvData());
        }
        if(csvWriter != null)
            csvWriter.close();
    }

    private static CsvWriter openCsv(OnePasswordItem opi, File outFile, List<OnePasswordItem> records) throws IOException {
        FileWriter fw = new FileWriter(outFile, true);
        fw.write("\n"+opi.getCsvOutputCategory()+"\n");
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.setNullValue("");
        settings.setQuoteAllFields(true);
        List<String> allHeaders = opi.getHeaders();
        String []headers = adjustHeaders(allHeaders, opi.getCsvOutputCategory(), records);
        settings.setHeaders(headers);
        CsvWriter writer = new CsvWriter(fw, settings);
        writer.writeHeaders();
        return writer;
    }

    private static String[] adjustHeaders(List<String> allHeaders, String csvOutputCategory, List<OnePasswordItem> records) {
        Set<String> headers = new HashSet<>();
        for(OnePasswordItem item : records){
            if(!item.getCsvOutputCategory().equalsIgnoreCase(csvOutputCategory))
                continue;
            Map<String, Object> csvData = item.getCsvData();
            for (Map.Entry<String, Object> stringObjectEntry : csvData.entrySet()) {
                if (stringObjectEntry.getValue() != null)
                    headers.add(stringObjectEntry.getKey());
            }
        }
        String []ret = new String[headers.size()];
        int idx = 0;
        boolean notesExists = false;
        for(String header : allHeaders){
            if(!headers.contains(header))
                continue;
            if(header.equals(OnePasswordItem.TAG_NOTES)){
                notesExists = true;
                continue;
            }
            ret[idx++] = header;
        }
        if(notesExists)
            ret[idx] = OnePasswordItem.TAG_NOTES;
        return ret;
    }

    private static void readItems(String itemId, List<OnePasswordItem> records) throws IOException, InterruptedException {
        String command = "op item get "+itemId+" --format json";
        String out = executeCommand(command);
        Map m = xs_objectMapper.readValue(out, Map.class);
        OnePasswordItem item = OnePasswordItem.getFromMap(m);
        records.add(item);
    }




    private static Set<String> readAllIds() throws Exception {
        log("Reading all ids from OP");
        Set<String> ret = new HashSet<>();
        String command = "op item list --format json";
        if(xs_vaultId != null)
            command+= " --vault "+ xs_vaultId;
        if(xs_tagToExport != null && (!xs_tagToExport.isEmpty()))
            command+= " --tags " + String.join(",", xs_tagToExport);
        String data = executeCommand(command);
        List<Map> outData = xs_objectMapper.readValue(data, List.class);
        for(Map m : outData){
            String id = (String) m.get("id");
            ret.add(id);
        }
        return ret;
    }

    static String executeCommand(String command) throws IOException, InterruptedException {
        log("Executing command : "+command);
        StringBuilder sb = new StringBuilder(8192);
        Process process = Runtime.getRuntime().exec(command);
        int code = process.waitFor();
        log("Command's exit code : "+code);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to read command's output : "+e.getMessage());
        }
        return sb.toString();
    }
    static void parseCli(String []args){
        try {
            for(int i=0; i<args.length; i++){
                if(args[i].equalsIgnoreCase("-tag")){
                    String allTags = args[i+1].toLowerCase();
                    xs_tagToExport = Arrays.asList(allTags.split(","));
                    i++;
                } else if(args[i].equalsIgnoreCase("-vault")){
                    xs_vaultId = args[i+1];
                    i++;
                } else if(args[i].equalsIgnoreCase("-outfile")){
                    xs_outFile = args[i+1];
                    i++;
                } else if(args[i].equalsIgnoreCase("-truncateUrl")){
                    xs_truncateUrl = true;

                } else outCommandLine();
            }
        } catch(ArrayIndexOutOfBoundsException e){
            outCommandLine();
        }
    }
    static void outCommandLine(){
        System.out.println("OPExport [-tag tagToExport1,tagToExport2...] [-vault vaultId] [-outfile outFileName=ot.csv] [-truncateUrl]");
        System.exit(1);
    }
    public static void log(String msg){
        System.out.println(LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" "+msg);
    }
    public static void err(String msg){
        System.err.println(LocalTime.now().truncatedTo(ChronoUnit.SECONDS)+" "+msg);
    }

}
