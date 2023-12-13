package ru.relex.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.relex.dao.AppUserDAO;
import ru.relex.dao.RawDataDAO;
import ru.relex.entity.AppUser;
import ru.relex.entity.RawData;
import ru.relex.entity.enums.UserRole;
import ru.relex.exceptions.QueryException;
import ru.relex.service.InvestService;
import ru.relex.service.MainService;
import ru.relex.service.ProducerService;
import ru.relex.service.enums.ServiceCommand;

import static ru.relex.service.enums.ServiceCommand.*;

@Log4j
@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final InvestService investService;

    public MainServiceImpl(RawDataDAO rawDataDAO,
                           ProducerService producerService,
                           AppUserDAO appUserDAO,
                           InvestService investService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.investService = investService;
    }

    @Override
    public void processTextMessage(Update update) {
        var chatId = update.getMessage().getChatId();
        try {
            saveRawData(update);
            var appUser = findOrSaveAppUser(update);
            var text = update.getMessage().getText();
            var output = "";
            output = processServiceCommand(appUser, text);
            sendAnswer(output, chatId);
        } catch (QueryException ex) {
            log.error(ex);
            String error = ex.getMessage();
            sendAnswer(error, chatId);
        }
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        String[] cmds = cmd.split(" ");
        var serviceCommand = ServiceCommand.fromValue(cmds[0]);
        if (GET_STOCKS.equals(serviceCommand) && UserRole.USER.equals(appUser.getRole()) && cmds.length > 1) {
            return investService.getStocksByCompany(cmds[1]);
        } else if (GET_BONDS.equals(serviceCommand) && UserRole.USER.equals(appUser.getRole()) && cmds.length > 1) {
            return investService.getBondsByCompany(cmds[1]);
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";
        } else {
            return "Неизвестная команда, либо вам не хватает прав для её исполнения! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд:\n"
                + "/get_stock Роснефть - для получения информации об акции Роснефти;\n"
                + "/get_bonds Роснефть - для получения информации об облигациях Роснефти;\n";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .role(UserRole.USER)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }

}
