package com.final_app.viewmodels;

import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.services.AppService;
import com.final_app.services.SpeakingTestService;
import com.final_app.tools.TranslationManager;
import com.final_app.views.components.SpeakingTestCard;
import com.final_app.views.pages.SpeakingTestPage;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import kotlin.properties.ObservableProperty;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class SpeakingTestsViewModel extends BaseViewModel {
    private final AppService appService = AppService.getInstance();
    private final SpeakingTestService speakingTestService = appService.getSpeakingTestService();

    private ObservableList<SpeakingTest> availableSpeakingTests = FXCollections.observableArrayList();
    private ObservableList<UserSpeakingTest> userSpeakingTests = FXCollections.observableArrayList();

    private ObservableMap<String, List<UserSpeakingTest>> groupedUserSpeakingTests = FXCollections.observableHashMap();
    public ObservableMap<String, List<UserSpeakingTest>> getGroupedUserSpeakingTests(){
        return this.groupedUserSpeakingTests;
    }

    public ObjectProperty<Settings> currentSettings = new SimpleObjectProperty<>();

    private ObservableList<Language> languages = FXCollections.observableArrayList();
    public ObservableList<Language> getLanguages() {
        return languages;
    }


    private BooleanProperty showNewPage = new SimpleBooleanProperty(false);
    public BooleanProperty showNewPageProperty(){
        return this.showNewPage;
    }


    public void initialize(){
        Thread thread = new Thread(() -> {
            if(appService.isAuthenticated()){
                try {
                    userSpeakingTests.setAll(speakingTestService.getUserSpeakingTests(appService.getCurrentUser().getId()));
                    groupedUserSpeakingTests.clear();
                    userSpeakingTests.forEach(userSpeakingTest -> {
                        if( groupedUserSpeakingTests.containsKey(userSpeakingTest.getTestId()) ){
                            groupedUserSpeakingTests.get(userSpeakingTest.getTestId()).add(userSpeakingTest);
                        }else{
                            groupedUserSpeakingTests.put(userSpeakingTest.getTestId(), new ArrayList<>(List.of(userSpeakingTest)));
                        }
                    });
                    availableSpeakingTests.setAll(speakingTestService.getAllSpeakingTests().stream().filter(test -> !userSpeakingTests.stream().map(UserSpeakingTest::getTestId).toList().contains(test.getId())).toList());
                    languages.setAll(appService.getLanguageService().getUserLanguages(appService.getCurrentUser().getId()).stream().map(UserLanguage::getLanguage).toList());

                    appService.getUserService().getUserSettings(getCurrentUser().getId())
                                    .thenAccept(settings -> {
                                        currentSettings.set(settings);
                                    });
                    reloadSystemText();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void togglePage(){
        this.showNewPage.set(!this.showNewPage.get());
        reloadSystemText();
    }

    public void reloadSystemText(){
        Platform.runLater(()->{
            lblTitleProperty.bind(TranslationManager.get().t(TKey.STITLE));
            lblDescriptionProperty.bind(TranslationManager.get().t(TKey.SDESCRIPTION));

            btnTextProperty.bind(TranslationManager.get().t(TKey.SNEWSPEAKINGTEST));

            lblSubTitleProperty.unbind();
            if(showNewPage.get()){
                lblSubTitleProperty.bind(TranslationManager.get().t(TKey.SASPEAKINGTESTS));
            }else{
                lblSubTitleProperty.bind(TranslationManager.get().t(TKey.SUSERSPEAKINGTESTS));
            }
        });
    }

    public ObservableList<SpeakingTest> getAvailableSpeakingTests(){
        return this.availableSpeakingTests;
    }
    public ObservableList<UserSpeakingTest> getUserSpeakingTests(){
        return this.userSpeakingTests;
    }

    public void navigateToTest(UserSpeakingTest userSpeakingTest){
        RootViewModel.getInstance().getNavigationService().navigate(SpeakingTestPage.class, vm ->{
            vm.setUserSpeakingTest(userSpeakingTest);
        }, true);
    }
    public void startSpeakingTest(SpeakingTest speakingTest){
        try {
            UserSpeakingTest userSpeakingTest = speakingTestService.startSpeakingTest(appService.getCurrentUser().getId(), speakingTest.getId());
            RootViewModel.getInstance().getNavigationService().navigateTo(SpeakingTestPage.class, vm -> {
                vm.setUserSpeakingTest(userSpeakingTest);
            });
        } catch (SQLException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onNavigatedTo() {
        this.initialize();
    }

    @Override
    public void onNavigatedFrom() {

    }

    // System text properties
    public StringProperty lblTitleProperty = new SimpleStringProperty();
    public StringProperty lblDescriptionProperty = new SimpleStringProperty();
    public StringProperty lblSubTitleProperty = new SimpleStringProperty();
    public StringProperty btnTextProperty = new SimpleStringProperty();
}
