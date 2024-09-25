package work.nullptr.compile.util;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class I18nConvertor {
    public static void main(String[] args) throws Exception {
        // 输出当前运行路径
        System.out.println(System.getProperty("user.dir"));
        // 简单检查参数
        if (args.length != 4) {
            System.err.println("请提供输入和输出文件路径.");
            return;
        }
        String excelPath = args[0];
        String outputDir = args[1];
        String baseName = args[2];
        String languages = args[3];

        System.out.println("excelPath: " + excelPath);
        System.out.println("outputDir: " + outputDir);
        System.out.println("baseName: " + baseName);
        System.out.println("languages: " + languages);

        DataFormatter formatter = new DataFormatter();
        String excelFilePath = Paths.get(excelPath).toAbsolutePath().toString();
        FileInputStream fis = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(OPCPackage.open(fis));
        Sheet sheet = workbook.getSheetAt(0);
        Properties codeKeyMap = new Properties();

        Map<String, Properties> propertiesMap = new HashMap<>();


        String[] language = languages.split(";");
        for (String s : language) {
            Properties properties = new Properties();
            propertiesMap.put(s, properties);
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String errorKey = formatter.formatCellValue(row.getCell(0));
            String errorCode = formatter.formatCellValue(row.getCell(1));

            propertiesMap.forEach((k, v) -> {
                String[] col = k.split(":");
                int colNum = Integer.parseInt(col[1]);
                String message = formatter.formatCellValue(row.getCell(colNum));
                v.put(errorKey, message);
            });
            codeKeyMap.setProperty(errorCode, errorKey);
        }


        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        for (Map.Entry<String, Properties> entry : propertiesMap.entrySet()) {
            String[] col = entry.getKey().split(":");
            String lang = col[0];
            if (lang.length() > 0) {
                lang = "_" + lang;
            }
            try (OutputStream outputStream = new FileOutputStream(outputPath.resolve(baseName + lang + ".properties").toFile())) {
                entry.getValue().store(outputStream, null);
            }
        }

        try (OutputStream outMap = new FileOutputStream(outputDir + "error_mapping.properties")) {
            codeKeyMap.store(outMap, null);
        }
    }
}
