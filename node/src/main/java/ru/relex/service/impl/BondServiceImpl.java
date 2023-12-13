package ru.relex.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.relex.exceptions.QueryException;
import ru.relex.service.BondService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j
@Service
@RequiredArgsConstructor
public class BondServiceImpl implements BondService {
    private final String BONDS_SELECTOR = "body > div.application > div > div.PageWrapper__wrapper_pqqYo > div > div > div > div:nth-child(2) > div:nth-child(3) > div.securitiesTable.Table__wrapper_KXIOF > div > table";

    @Override
    public String getBondByCompany(String companyName) {
        try {
            return getBondsDescription(companyName);
        } catch (Exception e) {
            log.error(e);
            throw new QueryException("Не удалось получить информацию по компании :(");
        }
    }


    private String getBondsDescription(String companyName) throws IOException {
        Document document = Jsoup.connect("https://www.tinkoff.ru/invest/recommendations/?query=" + companyName).userAgent("Mozilla").get();

        List<Map<String, String>> table = parseTable(document);
        clearTable(table);
        return printTable(table);
    }

    private List<Map<String, String>> parseTable(Document doc) {
        Elements res = doc.select(BONDS_SELECTOR + ":contains(Облигации)");
        Element table;
        if (!res.isEmpty()) {
            table = res.get(0);
        } else {
            table = doc.select("body > div.application > div > div.PageWrapper__wrapper_pqqYo > div > div > div > div:nth-child(2) > div:nth-child(4) > div.InvestCatalogSearchResults__title_eLg4D:contains(Облигации)").parents().get(0).child(1);
        }
        Elements rows = table.select("tr");
        Elements first = rows.get(0).select("th,td");

        List<String> headers = new ArrayList<>();
        for (Element header : first)
            headers.add(header.text());

        List<Map<String, String>> listMap = new ArrayList<>();
        for (int row = 1; row < rows.size(); row++) {
            Elements colVals = rows.get(row).select("th,td");
            int colCount = 0;
            Map<String, String> tuple = new HashMap<>();
            for (Element colVal : colVals)
                tuple.put(headers.get(colCount++), colVal.text());
            listMap.add(tuple);
        }
        return listMap;
    }

    public String clearTable(List<Map<String, String>> table) {
        for (Map<String, String> map : table) {
            for (String key : map.keySet()) {
                if (key.equals("Дата погашения")) {
                    map.put(key, map.get(key).split(" на")[0]);
                }
            }
            map.entrySet().removeIf(entry -> entry.getKey().equals("Название"));
        }


        return table.toString();
    }

    public String printTable(List<Map<String, String>> table) {
        StringBuilder builder = new StringBuilder();
        for (String key : table.get(0).keySet()) {
            builder.append(String.format("%20s", key));
        }
        builder.append("\n\n");
        for (Map<String, String> map : table) {
            for (String value : map.values()) {
                builder.append(String.format("%20s", value));
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
