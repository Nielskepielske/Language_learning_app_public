package com.final_app.views.pages;

import com.final_app.globals.ConversationStatus;
import com.final_app.globals.GlobalVariables;
import com.final_app.globals.TKey;
import com.final_app.models.*;
import com.final_app.tools.AnimationUtils;
import com.final_app.tools.ListFilter;
import com.final_app.tools.SVGUtil;
import com.final_app.tools.TranslationManager;
import com.final_app.viewmodels.ConversationsViewModel;
import com.final_app.viewmodels.RootViewModel;
import com.final_app.viewmodels.SpeakingTestsViewModel;
import com.final_app.views.components.CustomGroupWindow;
import com.final_app.views.components.SpeakingTestCard;
import com.final_app.views.components.custom.general.FilterWindow;
import com.final_app.views.components.custom.general.SearchBar;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class SpeakingTestsView implements FxmlView<SpeakingTestsViewModel> {
    @FXML private FlowPane cards;
    @FXML private HBox btnAddNewSpeakingTest;
    @FXML private Label lblSubTitle;

    // System text elements
    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblNewSpeakingTest;

    // filters
    @FXML private SearchBar searchBar;
    @FXML private HBox btnFilter;
    @FXML private ImageView imgFilter;
    @FXML private FilterWindow filterWindow;

    // Grouped window
    @FXML private CustomGroupWindow<UserSpeakingTest> groupedSpeakingTestWindow;

    private UserSpeakingTest selectedSpeakingTest;

    private List<Language> selectedLanguages = new ArrayList<>();

    @InjectViewModel
    private SpeakingTestsViewModel viewModel;


    public void initialize(){
        loadLists();
        bindUIText();
        viewModel.showNewPageProperty().addListener((obs, oldVal, newVal)->{
            loadLists();
        });

        viewModel.getGroupedUserSpeakingTests().addListener((MapChangeListener<? super String, ? super List<UserSpeakingTest>>) e -> {
            if(groupedSpeakingTestWindow.isVisible() && !viewModel.getGroupedUserSpeakingTests().isEmpty() && selectedSpeakingTest != null){
                if( viewModel.getGroupedUserSpeakingTests().get(selectedSpeakingTest.getTestId()) != null && !viewModel.getGroupedUserSpeakingTests().get(selectedSpeakingTest.getTestId()).isEmpty()){
                    Platform.runLater(()->{
                        groupedSpeakingTestWindow.initializeRow(viewModel.getGroupedUserSpeakingTests().get(selectedSpeakingTest.getTestId()));
                    });
                }
            }
        });

        viewModel.getUserSpeakingTests().addListener((ListChangeListener<? super UserSpeakingTest>) e -> {
            loadLists();
        });

        btnAddNewSpeakingTest.setOnMouseClicked(_ ->{
            viewModel.togglePage();
        });
        setBindings();
    }
    private void setBindings(){
        filterWindow.setLanguageKeys(TKey.FWLTITLE, TKey.FWLDESCRIPTION);
        imgFilter.setImage(SVGUtil.loadSVG(GlobalVariables.ICONS + "filter_light.svg", 20, 20) );
        btnFilter.setOnMouseClicked(e -> {
            filterWindow.setVisible(!filterWindow.isVisible());
            if(filterWindow.isVisible()){
                filterWindow.setItems(viewModel.getLanguages());
            }
        });
        filterWindow.setOnCancel(_ -> {
            filterWindow.setVisible(false);
        });
        filterWindow.setOnApply(lang -> {
            selectedLanguages = lang;
            filterWindow.setVisible(false);
            loadLists();
        });

        searchBar.setOnSearch(text -> {
           loadLists();
        });

        viewModel.currentSettings.addListener((obs, oldVal, newVal)->{
            loadLists();
        });
    }

    private void bindUIText(){
        lblTitle.textProperty().bind(viewModel.lblTitleProperty);
        lblDescription.textProperty().bind(viewModel.lblDescriptionProperty);
        lblSubTitle.textProperty().bind(viewModel.lblSubTitleProperty);
        lblNewSpeakingTest.textProperty().bind(viewModel.btnTextProperty);
    }

    private void loadLists(){
        Platform.runLater(()->{
            if(viewModel.showNewPageProperty().get()){
                AnimationUtils.fadeOut(cards, Duration.millis(500), ()->{
                    cards.getChildren().clear();
                    cards.setOpacity(1);
                    //AnimationUtils.fadeInSlideIn(cards, Duration.millis(500), 300, 0, null);
                    viewModel.lblSubTitleProperty.unbind();
                    viewModel.lblSubTitleProperty.bind(TranslationManager.get().t(TKey.SASPEAKINGTESTS));
                    filteredList(viewModel.getAvailableSpeakingTests()).forEach(item -> {
                        SpeakingTest test;
                        if(item instanceof SpeakingTest){
                            test = (SpeakingTest)item;
                        }else{
                            test = null;
                            return;
                        }
                        SpeakingTestCard<SpeakingTest> card = new SpeakingTestCard<>(test);
                        cards.getChildren().add(card);

                        AnimationUtils.fadeInSlideIn(card, Duration.millis(300), 200, 0, null);

                        card.setOnButtonClick(_ ->{
                            viewModel.startSpeakingTest(test);
                        });
                    });
                });

            }else{
                AnimationUtils.fadeOut(cards, Duration.millis(500), ()->{
                    cards.getChildren().clear();
                    //AnimationUtils.fadeInSlideIn(cards, Duration.millis(500), 300, 0, null);
//                    cards.setVisible(true);
//                    cards.setOpacity(1);
                    //AnimationUtils.fadeIn(cards, Duration.millis(10), null);
                    cards.setOpacity(1);
                    //viewModel.lblSubTitleProperty.set(TranslationManager.get().t(TKey.SUSERSPEAKINGTESTS));
                    viewModel.getGroupedUserSpeakingTests().forEach((id, list) -> {
                        if(filteredList(list) == null || filteredList(list).isEmpty()) return;
                        UserSpeakingTest userSpeakingTest = list.getFirst();
                        SpeakingTestCard<UserSpeakingTest> card = new SpeakingTestCard<>(userSpeakingTest);
                        cards.getChildren().add(card);


                        AnimationUtils.fadeInSlideIn(card, Duration.millis(300), 200, 0, null);
                        //AnimationUtils.slideIn(card, Duration.millis(1000), -300, 0, null);

                        card.setOnButtonClick(_ ->{
                            if(list.size() > 1 || list.getFirst().getStatusEnum() == ConversationStatus.COMPLETED){
                                selectedSpeakingTest = userSpeakingTest;
                                groupedSpeakingTestWindow.initializeRow(list);
                                groupedSpeakingTestWindow.setVisible(true);

                                groupedSpeakingTestWindow.setOnBackButtonClicked(e -> {
                                    groupedSpeakingTestWindow.setVisible(false);
                                });
                                groupedSpeakingTestWindow.setOnCreateButtonClicked(e -> {
                                    viewModel.startSpeakingTest(userSpeakingTest.getTest());
                                });
                            }else{
                                viewModel.navigateToTest(userSpeakingTest);
                            }
                        });
                    });
                });


            }
        });

    }

    private List<?> filteredList(List<?> list){
        if(list.isEmpty()) return list;
        if(list.getFirst() instanceof UserSpeakingTest){
            Predicate<UserSpeakingTest> userSpeakingTestPredicate = item -> searchBar.getSearchFieldText().isEmpty() || item.getTest().getTitle().toLowerCase().contains(searchBar.textProperty.get().toLowerCase());
            Predicate<UserSpeakingTest> userSpeakingTestPredicate2 = item -> selectedLanguages.isEmpty() || selectedLanguages.contains(item.getTest().getLanguage());
            Predicate<UserSpeakingTest> userSpeakingTestPredicate3 = item -> viewModel.currentSettings.get() == null || viewModel.currentSettings.get().getSelectedLanguages().isEmpty() || (viewModel.currentSettings.get().getSelectedLanguages().size() == 1 && viewModel.currentSettings.get().getSelectedLanguages().getFirst().isEmpty()) || viewModel.currentSettings.get().getSelectedLanguages().contains(item.getTest().getLanguageFromId());
            return ListFilter.filterList((List<UserSpeakingTest>)list, userSpeakingTestPredicate, userSpeakingTestPredicate2, userSpeakingTestPredicate3);
        }else if(list.getFirst() instanceof SpeakingTest) {
            Predicate<SpeakingTest> speakingTestPredicate = item -> searchBar.getSearchFieldText().isEmpty() || item.getTitle().toLowerCase().contains(searchBar.textProperty.get().toLowerCase());
            Predicate<SpeakingTest> speakingTestPredicate2 = item -> selectedLanguages.isEmpty() || selectedLanguages.contains(item.getLanguage());
            Predicate<SpeakingTest> speakingTestPredicate3 = item -> viewModel.currentSettings.get() == null || viewModel.currentSettings.get().getSelectedLanguages().isEmpty() || (viewModel.currentSettings.get().getSelectedLanguages().size() == 1 && viewModel.currentSettings.get().getSelectedLanguages().getFirst().isEmpty()) || viewModel.currentSettings.get().getSelectedLanguages().contains(item.getLanguageFromId());
            return ListFilter.filterList((List<SpeakingTest>) list, speakingTestPredicate, speakingTestPredicate2, speakingTestPredicate3);
        }
        return list;
    }
}
