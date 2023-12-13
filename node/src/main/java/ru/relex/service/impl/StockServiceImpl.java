package ru.relex.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import ru.relex.exceptions.QueryException;
import ru.relex.service.StockService;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;

@Log4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final String TIKCER_XPATH = "/html/body/div[1]/div/div[2]/div/div/div/div[2]/div[2]/div[2]/table/tbody/tr/td[1]/a/span/div/div/div/div[1]/div[2]/div/text()";
    private final String STOCK_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]";
    private final String COMPANY_NAME_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[1]/div[1]/div[1]/h2/span[1]/text()";
    private final String COMPANY_INFO_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[3]/div[2]/div[1]/div/div/p[1]/text()";
    private final String STOCK_PRICE_INT_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[2]/div/div/div[1]/div/div/div[2]/span/span/text()";
    private final String STOCK_PRICE_DOUBLE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[2]/div/div/div[1]/div/div/div[2]/span/span/span/text()";
    private final String HALF_YEAR_PROFITABILITY_INT_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[1]/div[1]/div[2]/div[1]/div[2]/span/text()";
    private final String HALF_YEAR_PROFITABILITY_DOUBLE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[1]/div[1]/div[2]/div[1]/div[2]/span/span/text()";
    private final String SECTOR_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[1]/div[1]/div[2]/div[2]/div[2]/text()";
    private final String FORECAST_INT_PRICE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[1]/div[2]/div/span/text()";
    private final String FORECAST_DOUBLE_PRICE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[1]/div[2]/div/span/span/text()";
    private final String FORECAST_DIFFERENCE_INT_PRICE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[1]/div[2]/span/div/span[1]/text()";
    private final String FORECAST_DIFFERENCE_DOUBLE_PRICE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[1]/div[2]/span/div/span[1]/span/text()";
    private final String FORECAST_DIFFERENCE_PROCENT_INT_PRICE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[1]/div[2]/span/div/span[2]/text()";
    private final String FORECAST_DIFFERENCE_PROCENT_DOUBLE_PRICE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[1]/div[2]/span/div/span[2]/span/text()";
    private final String LOW_RANGE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[2]/div[2]/span[1]/text()";
    private final String LOW_RANGE_SIGN_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[2]/div[2]/span[1]/span/text()";
    private final String RANGE_SIGN_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[2]/div[2]/text()";
    private final String HIGH_RANGE_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[2]/div[2]/span[2]/text()";
    private final String HIGH_RANGE_SIGN_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[2]/div[2]/span[2]/span/text()";
    private final String RECOMMENDATION_XPATH = "/html/body/div[1]/div/div[2]/div/div[1]/div/div[2]/div[1]/div[4]/div[5]/div[2]/div[3]/div[2]/text()";

    @Override
    public String getStockByCompany(String companyName) {
        try {
            String ticker = getTicker(companyName);
            if (ticker.isBlank()) {
                throw new QueryException("Не удалось получить информацию по компании :(");
            }
            return getStockDescription(ticker);
        } catch (Exception e) {
            log.error(e);
            throw new QueryException("Не удалось получить информацию по компании :(");
        }
    }


    private String getTicker(String companyName) throws IOException {
        Document document = Jsoup.connect("https://www.tinkoff.ru/invest/recommendations/?query=" + companyName).userAgent("Mozilla").get();
        return Xsoup.select(document, TIKCER_XPATH).get();
    }

    private String getStockDescription(String ticker) throws IOException {
        Document document = Jsoup.connect("https://www.tinkoff.ru/invest/stocks/" + ticker + "/").userAgent("Mozilla").get();
        StringBuilder stockInfo = new StringBuilder();
        stockInfo.append(Xsoup.select(document, COMPANY_NAME_XPATH).get() + "\n\n");
        stockInfo.append("Сектор: ");
        getFieldInfo(document, stockInfo, SECTOR_XPATH);
        stockInfo.append("\n");
        stockInfo.append("О компании:\n");
        getFieldInfo(document, stockInfo, COMPANY_INFO_XPATH);
        stockInfo.append("\n\n");
        stockInfo.append("Цена акции на сегодня: ");
        getFieldInfo(document, stockInfo, STOCK_PRICE_INT_XPATH);
        getFieldInfo(document, stockInfo, STOCK_PRICE_DOUBLE_XPATH);
        stockInfo.append("\n");
        stockInfo.append("Доходность за полгода: ");
        getFieldInfo(document, stockInfo, HALF_YEAR_PROFITABILITY_INT_XPATH);
        getFieldInfo(document, stockInfo, HALF_YEAR_PROFITABILITY_DOUBLE_XPATH);
        stockInfo.append("%");
        stockInfo.append("\n\n");
        stockInfo.append("Сводный прогноз\n");
        stockInfo.append("Прогнозная цена: ");
        getFieldInfo(document, stockInfo, FORECAST_INT_PRICE_XPATH);
        getFieldInfo(document, stockInfo, FORECAST_DOUBLE_PRICE_XPATH);
        stockInfo.append("\n");
        stockInfo.append("Прогнозное изменение цены в процентах: ");
        getFieldInfo(document, stockInfo, FORECAST_DIFFERENCE_INT_PRICE_XPATH);
        getFieldInfo(document, stockInfo, FORECAST_DIFFERENCE_DOUBLE_PRICE_XPATH);
        stockInfo.append("(");
        getFieldInfo(document, stockInfo, FORECAST_DIFFERENCE_PROCENT_INT_PRICE_XPATH);
        getFieldInfo(document, stockInfo, FORECAST_DIFFERENCE_PROCENT_DOUBLE_PRICE_XPATH);
        stockInfo.append("%)\n");
        stockInfo.append("Диапазон:\n");
        getFieldInfo(document, stockInfo, LOW_RANGE_XPATH);
        getFieldInfo(document, stockInfo, LOW_RANGE_SIGN_XPATH);
        getFieldInfo(document, stockInfo, RANGE_SIGN_XPATH);
        getFieldInfo(document, stockInfo, HIGH_RANGE_XPATH);
        getFieldInfo(document, stockInfo, HIGH_RANGE_SIGN_XPATH);
        stockInfo.append("\n");
        stockInfo.append("Рекомендация: ");
        getFieldInfo(document, stockInfo, RECOMMENDATION_XPATH);

        return stockInfo.toString();
    }

    private void getFieldInfo(Document document, StringBuilder builder, String xpath) {
        String text = Xsoup.select(document, STOCK_XPATH).get();
        if (text.isBlank()) {
            builder.append("не удалось найти информацию");
        } else {
            builder.append(Xsoup.select(document, xpath).get());
        }
    }
}
