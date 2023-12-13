package ru.relex.service;

public interface InvestService {
    String getStocksByCompany(String companyName);
    String getBondsByCompany(String companyName);

}
