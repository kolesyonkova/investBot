package ru.relex.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import ru.relex.service.BondService;
import ru.relex.service.InvestService;
import ru.relex.service.StockService;

@Log4j
@Service
@RequiredArgsConstructor
public class InvestServiceImpl implements InvestService {
    private final StockService stockService;
    private final BondService bondService;

    @Override
    public String getStocksByCompany(String companyName) {
        return stockService.getStockByCompany(companyName);
    }

    @Override
    public String getBondsByCompany(String companyName) {
        return bondService.getBondByCompany(companyName);
    }
}
