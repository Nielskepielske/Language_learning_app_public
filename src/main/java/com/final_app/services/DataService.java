package com.final_app.services;

import com.final_app.db.DatabaseManager;
import com.final_app.db.dao.*;
import com.final_app.factories.RepositoryFactory;
import com.final_app.globals.*;
import com.final_app.interfaces.*;
import com.final_app.models.LanguageLevel;
import com.final_app.models.*;
import com.final_app.tools.PasswordUtil;
import com.final_app.tools.TranslationManager;

import javax.swing.tree.TreeNode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for initializing and managing application data
 */
public class DataService {
    // DAOs
    private final ILanguageRepository languageRepository = RepositoryFactory.getLanguageRepository();
    private final IScenarioRepository scenarioRepository = RepositoryFactory.getScenarioRepository();
    private final IConversationRepository conversationRepository = RepositoryFactory.getConversationRepository();
    private final IUserRepository userRepository = RepositoryFactory.getUserRepository();

    private Language defaultLanguage = new Language(null, "English", "en", "#000000", 0);

    /**
     * Initialize the database with default data
     */
    public void initializeDefaultData() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            //resetAllData();

            System.out.println("Checking for existing data...");
            List<Language> languages = (List<Language>) languageRepository.getAllLanguages().join();
            //System.out.println("Found " + languages.size() + " languages");


            System.out.println("Initializing languages...");
            initializeLanguages();
            if(!conversationRepository.getAllConversations().join().iterator().hasNext()){
                System.out.println("Initializing conversations...");
                initializeConversations();
            }


            // TODO: Terug inschakelen
            if(!userRepository.getAllUsers().join().iterator().hasNext()){
                System.out.println("Initializing sample user data...");
                initializeSampleUserData();
            }

            initializeBaseTranslations();



            conn.commit();
            System.out.println("Database initialization completed successfully!");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            throw e;
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    private void initializeBaseTranslations() throws SQLException{
        Language eng = new Language(null, "English", "en", "#000000", 0);
        try {
            // Dashboard page
            TranslationManager.get().addTranslation(eng, TKey.DWELCOME, "Welcome back,");
            TranslationManager.get().addTranslation(eng, TKey.DEXPLANATION, "Track your progress and continue your language learning journey.");
            TranslationManager.get().addTranslation(eng, TKey.DLEVELTITLE, "Your Level");
            TranslationManager.get().addTranslation(eng, TKey.DLEVELDESCRIPTION, "Keep learning to level up and unlock new features");
            TranslationManager.get().addTranslation(eng, TKey.DLEVELPROGRESSH, "Level");
            TranslationManager.get().addTranslation(eng, TKey.DADDLANGUAGE, "Add new language");
            TranslationManager.get().addTranslation(eng, TKey.DCREATEITEMS, "Create Items");
            TranslationManager.get().addTranslation(eng, TKey.DLANGUAGEPTITLE, "Your Language Progress");
            TranslationManager.get().addTranslation(eng, TKey.DLANGUAGEPDESCRIPTION, "Track your proficiency in each language");
            TranslationManager.get().addTranslation(eng, TKey.DTOTALXP, "Total XP");
            TranslationManager.get().addTranslation(eng, TKey.DDAYSTREAK, "Day Streak");

            // General
            TranslationManager.get().addTranslation(eng, TKey.CONVERSATIONS, "Conversations");
            TranslationManager.get().addTranslation(eng, TKey.SPEAKINGTESTS, "Speaking Tests");
            TranslationManager.get().addTranslation(eng, TKey.THISWEEK, "this week");
            TranslationManager.get().addTranslation(eng, TKey.DAYS, "days");
            TranslationManager.get().addTranslation(eng, TKey.XPTO, "XP to");
            TranslationManager.get().addTranslation(eng, TKey.CANCEL, "Cancel");
            TranslationManager.get().addTranslation(eng, TKey.SEARCH, "Search");
            TranslationManager.get().addTranslation(eng, TKey.APPLY, "Apply");
            TranslationManager.get().addTranslation(eng, TKey.GENERATE, "Generate");
            TranslationManager.get().addTranslation(eng, TKey.AVERAGESCORE, "Average Score");
            TranslationManager.get().addTranslation(eng, TKey.SELECTEDLANGUAGES, "Selected Languages");

            // Navigation Items
            TranslationManager.get().addTranslation(eng, TKey.DASHBOARD, "Dashboard");
            TranslationManager.get().addTranslation(eng, TKey.SETTINGS, "Settings");
            TranslationManager.get().addTranslation(eng, TKey.LOGOUT, "Logout");

            // Conversation page
            TranslationManager.get().addTranslation(eng, TKey.CTITLE, "Your Conversations");
            TranslationManager.get().addTranslation(eng, TKey.CDESCRIPTION, "Continue your language practice sessions");
            TranslationManager.get().addTranslation(eng, TKey.CCONVERSATIONCHAINS, "Your Conversation Chains");
            TranslationManager.get().addTranslation(eng, TKey.CNEWCONVERSATION, "New Conversation");
            TranslationManager.get().addTranslation(eng, TKey.CACONVERSATIONCHAINS, "Available Conversation Chains");
            TranslationManager.get().addTranslation(eng, TKey.CACONVERSATIONS, "Available Conversations");

            // Speaking Tests
            TranslationManager.get().addTranslation(eng, TKey.STITLE, "Speaking Tests");
            TranslationManager.get().addTranslation(eng, TKey.SDESCRIPTION, "Test your speaking abilities and pronunciation");
            TranslationManager.get().addTranslation(eng, TKey.SUSERSPEAKINGTESTS, "Your Speaking Tests");
            TranslationManager.get().addTranslation(eng, TKey.SNEWSPEAKINGTEST, "New Speaking Test");
            TranslationManager.get().addTranslation(eng, TKey.SASPEAKINGTESTS, "Available Speaking Tests");
            TranslationManager.get().addTranslation(eng, TKey.LESSON, "Lesson");
            TranslationManager.get().addTranslation(eng, TKey.STARTTEST, "Start test");

            // Settings
            TranslationManager.get().addTranslation(eng, TKey.SETDESCRIPTION, "Manage your account settings and preferences");
            TranslationManager.get().addTranslation(eng, TKey.SETACCOUNTINFO, "Account Information");
            TranslationManager.get().addTranslation(eng, TKey.SETACCOUNTDESCRIPTION, "Manage your account settings");
            TranslationManager.get().addTranslation(eng, TKey.SETACCOUNTUN, "Username:");
            TranslationManager.get().addTranslation(eng, TKey.SETACCOUNTEM, "Email:");
            TranslationManager.get().addTranslation(eng, TKey.SETSAVE, "Save");
            TranslationManager.get().addTranslation(eng, TKey.SETLANGUAGETITLE, "Language Settings");
            TranslationManager.get().addTranslation(eng, TKey.SETLANGUAGEDES, "Choose the system language. This includes translations as well.");
            TranslationManager.get().addTranslation(eng, TKey.SETLANGUAGESUB, "Language");

            // Conversation Chain page
            TranslationManager.get().addTranslation(eng, TKey.CCHPROGRESS, "Progress");
            TranslationManager.get().addTranslation(eng, TKey.CCHDESCRIPTIONTITLE, "Chain progress");
            TranslationManager.get().addTranslation(eng, TKey.CCHSUBTITLE, "Conversations in this chain");

            // Chatview page
            TranslationManager.get().addTranslation(eng, TKey.CHKEYPOINTSTOPRACTICE, "Keypoints to practice");

            // New Language page
            TranslationManager.get().addTranslation(eng, TKey.NLTITLE, "Add New Language");
            TranslationManager.get().addTranslation(eng, TKey.NLDESCRIPTION, "Expand on your language learning journey");
            TranslationManager.get().addTranslation(eng, TKey.NLSELECTLANGUAGE, "Select a Language");
            TranslationManager.get().addTranslation(eng, TKey.NLSELECTLANGUAGEDES, "Choose a language to start learning");
            TranslationManager.get().addTranslation(eng, TKey.NLPROFICIENCY, "Proficiency");
            TranslationManager.get().addTranslation(eng, TKey.NLPROFICIENCYDES, "Select the proficiency level for this language");
            TranslationManager.get().addTranslation(eng, TKey.NLSUMMARY, "Summary");
            TranslationManager.get().addTranslation(eng, TKey.NLSUMMARYDES, "Review your selection");
            TranslationManager.get().addTranslation(eng, TKey.NLCOMFIRMBTN, "Add Language");

            // Create items form
            TranslationManager.get().addTranslation(eng, TKey.FTITLE, "Create New Item");
            TranslationManager.get().addTranslation(eng, TKey.FDESCRIPTION, "Add new content to your learning journey");
            TranslationManager.get().addTranslation(eng, TKey.FSUBTITLE, "What would you like to add?");
            TranslationManager.get().addTranslation(eng, TKey.FCONVERSATIONCHAIN, "Conversation Chain");
            TranslationManager.get().addTranslation(eng, TKey.FCONVERSATION, "Conversation");
            TranslationManager.get().addTranslation(eng, TKey.FSCENARIO, "Scenario");
            TranslationManager.get().addTranslation(eng, TKey.FSPEAKINGTEST, "Speaking test");
            TranslationManager.get().addTranslation(eng, TKey.FPTITLE, "Title");
            TranslationManager.get().addTranslation(eng, TKey.FPDESCRIPTION, "Description");
            TranslationManager.get().addTranslation(eng, TKey.FPLANGUAGE, "Language");
            TranslationManager.get().addTranslation(eng, TKey.FPLANGUAGEFROM, "Language from");
            TranslationManager.get().addTranslation(eng, TKey.FPLANGUAGELEVEL, "Language Level");
            TranslationManager.get().addTranslation(eng, TKey.FPADD, "Add");
            TranslationManager.get().addTranslation(eng, TKey.FPCREATE,"Create");
            TranslationManager.get().addTranslation(eng, TKey.FPEXPLANATION, "Explanation");
            TranslationManager.get().addTranslation(eng, TKey.FPKEYPOINTS, "Keypoints");
            TranslationManager.get().addTranslation(eng, TKey.FPROLE, "Role");
            TranslationManager.get().addTranslation(eng, TKey.FPSTARTPROMPT, "Start Prompt");
            TranslationManager.get().addTranslation(eng, TKey.FPKEYPOINT, "Keypoint");
            TranslationManager.get().addTranslation(eng, TKey.FPKEYPOINTHEADER, "Add Keypoint");

            // Specific messages for forms
            // Conversation Chain
            TranslationManager.get().addTranslation(eng, TKey.CCFDESCRIPTION, "Fill in the details to create a new conversation chain");
            TranslationManager.get().addTranslation(eng, TKey.CCFDESCRIPTIONNAV, "A series of related conversations");
            TranslationManager.get().addTranslation(eng, TKey.FPADDCONVERSATIONSTITLE, "Add Conversations");
            TranslationManager.get().addTranslation(eng, TKey.FPADDCONVERSATIONSDESCRIPTION, "Choose which conversations to add to this conversation chain");

            // Conversation
            TranslationManager.get().addTranslation(eng, TKey.CFDESCRIPTION, "Fill in the details to create a new conversation");
            TranslationManager.get().addTranslation(eng, TKey.CFDESCRIPTIONNAV, "Practice speaking with an AI partner");

            // Speaking Test
            TranslationManager.get().addTranslation(eng, TKey.SFDESCRIPTIONSPEAKINGTEST, "Fill in the details to create a new speaking test");
            TranslationManager.get().addTranslation(eng, TKey.SFDESCRIPTIONSPEAKINGTESTNAV, "Test your speaking abilities");

            // Scenario
            TranslationManager.get().addTranslation(eng, TKey.SFDESCRIPTION, "Fill in the details to create a new scenario");
            TranslationManager.get().addTranslation(eng, TKey.SFDESCRIPTIONNAV, "Create a scenario for your conversation");

            // Filter Window
            // Language
            TranslationManager.get().addTranslation(eng, TKey.FWLTITLE, "Languages");
            TranslationManager.get().addTranslation(eng, TKey.FWLDESCRIPTION, "Select the language you want to filter by");

            // Group window
            TranslationManager.get().addTranslation(eng, TKey.CREATENEW, "Create New");
            TranslationManager.get().addTranslation(eng, TKey.COLSTATUS, "Status");
            TranslationManager.get().addTranslation(eng, TKey.COLDATE, "Date");
            TranslationManager.get().addTranslation(eng, TKey.COLSCORE, "Score");
            TranslationManager.get().addTranslation(eng, TKey.COLDURATION, "Duration");
            TranslationManager.get().addTranslation(eng, TKey.COLACTION, "Action");
            TranslationManager.get().addTranslation(eng, TKey.COMPLETED, "Completed");
            TranslationManager.get().addTranslation(eng, TKey.INPROGRESS, "In Progress");
            TranslationManager.get().addTranslation(eng, TKey.NOTSTARTED, "Not Started");
            TranslationManager.get().addTranslation(eng, TKey.REVIEW, "Review");
            TranslationManager.get().addTranslation(eng, TKey.CONTINUE, "Continue");
            TranslationManager.get().addTranslation(eng, TKey.START, "Start");

            // Evaluation window
            TranslationManager.get().addTranslation(eng, TKey.EVTITLEC, "Evaluation for conversation");
            TranslationManager.get().addTranslation(eng, TKey.EVTITLES, "Evaluation for speaking test");
            TranslationManager.get().addTranslation(eng, TKey.EVTITLEN, "Evaluation");
            TranslationManager.get().addTranslation(eng, TKey.EVFEEDBACK, "Feedback");
            TranslationManager.get().addTranslation(eng, TKey.EVSTATSTITLE, "Skill breakdown");
            TranslationManager.get().addTranslation(eng, TKey.EVBTNSHOWFEEDBACK, "Show Feedback");
            TranslationManager.get().addTranslation(eng, TKey.EVBTNSHOWSTATS, "Show Stats");
            TranslationManager.get().addTranslation(eng, TKey.EVBTNNEXT, "Next");
            TranslationManager.get().addTranslation(eng, TKey.EVVOCAB, "Vocabulary");
            TranslationManager.get().addTranslation(eng, TKey.EVGRAMMAR, "Grammar");


            TranslationManager.get().updateDefaultTranslations();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize sample user data including user conversations
     */
    private void initializeSampleUserData() throws SQLException {
        try{
            // Create a sample user if none exists
            List<User> users = (List<User>) userRepository.getAllUsers().join();
            User sampleUser;

            if (users.isEmpty()) {
                String password = PasswordUtil.simpleHash("password123");
                sampleUser = new User("sampleuser", "user@example.com", password, null);
                userRepository.addUser(sampleUser).join();

                // Create user stats
                UserStats stats = new UserStats(sampleUser.getId(), 1, 0, 0);
                userRepository.saveUserStats(sampleUser, stats);
            } else {
                sampleUser = users.get(0);
            }

            // Get some conversations to create samples for
            List<Conversation> conversations = (List<Conversation>) conversationRepository.getAllConversations().join();
            if (conversations.size() < 5) {
                return; // Not enough conversations to create samples
            }

            // Create sample user conversations in different states
            IUserConversationsRepository userConversationsRepository = RepositoryFactory.getUserConversationsRepository();
            IMessageRepository messageRepository = RepositoryFactory.getMessageRepository();
            IEvaluationRepository evaluationRepository = RepositoryFactory.getEvaluationRepository();

            // 1. Completed French Restaurant conversation
            Conversation frenchRestaurant = null;
            for (Conversation conv : conversations) {
                if (conv.getTitle().equals("Restaurant Ordering") &&
                        conv.getLanguage().getName().equals("French")) {
                    frenchRestaurant = conv;
                    break;
                }
            }

            if (frenchRestaurant != null) {
                // Check if this user conversation already exists
                UserConversation existingConvo = userConversationsRepository.getUserConversationsByUserAndConversationId(
                        sampleUser.getId(), frenchRestaurant.getId()).thenApply(userConversations -> {
                            if(userConversations.iterator().hasNext())
                                return (UserConversation) userConversations.iterator().next();
                            else
                                return null;
                }).join();

                if (existingConvo == null) {
                    // Create a new completed French restaurant conversation
                    UserConversation userConvo = new UserConversation(
                            sampleUser.getId(), frenchRestaurant.getId(), ConversationStatus.COMPLETED.name());
                    userConversationsRepository.addUserConversation(userConvo);

                    // Add a sequence of messages
                    messageRepository.addMessage(new Message(0, userConvo.getId(),
                            "Bonjour, je voudrais une table pour une personne, s'il vous plaît.", "USER"));
                    messageRepository.addMessage(new Message(1, userConvo.getId(),
                            "Bonjour! Bien sûr, suivez-moi. Voici votre table. Voulez-vous voir notre menu?", "AI"));
                    messageRepository.addMessage(new Message(2, userConvo.getId(),
                            "Oui, merci. Qu'est-ce que vous recommandez?", "USER"));
                    messageRepository.addMessage(new Message(3, userConvo.getId(),
                            "Je recommande le plat du jour, c'est un boeuf bourguignon. C'est très délicieux!", "AI"));
                    messageRepository.addMessage(new Message(4, userConvo.getId(),
                            "Ça semble bon. Je vais prendre ça et un verre de vin rouge, s'il vous plaît.", "USER"));
                    messageRepository.addMessage(new Message(5, userConvo.getId(),
                            "Très bon choix! Je vous apporte ça tout de suite.", "AI"));
                    messageRepository.addMessage(new Message(6, userConvo.getId(),
                            "Merci beaucoup.", "USER"));
                    messageRepository.addMessage(new Message(7, userConvo.getId(),
                            "Voilà votre repas. Bon appétit!", "AI"));
                    messageRepository.addMessage(new Message(8, userConvo.getId(),
                            "C'est délicieux! L'addition, s'il vous plaît.", "USER"));
                    messageRepository.addMessage(new Message(9, userConvo.getId(),
                            "Bien sûr, voici l'addition. Ça fait 35 euros.", "AI"));
                    messageRepository.addMessage(new Message(10, userConvo.getId(),
                            "Voici 40 euros. Gardez la monnaie.", "USER"));
                    messageRepository.addMessage(new Message(11, userConvo.getId(),
                            "Merci beaucoup! Bonne journée!", "AI"));

                    // Add an evaluation
                    Evaluation eval = new Evaluation(userConvo.getId(), 85, 100, 4, 3,
                            "Good job with restaurant vocabulary! Your sentence structure is generally correct, " +
                                    "but work on verb conjugation. Try using more adjectives to describe food.");
                    evaluationRepository.addEvaluation(eval);
                }
            }

            // 2. In-progress English Weather conversation
            Conversation englishWeather = null;
            for (Conversation conv : conversations) {
                if (conv.getTitle().equals("Talking About Weather") &&
                        conv.getLanguage().getName().equals("English")) {
                    englishWeather = conv;
                    break;
                }
            }

            if (englishWeather != null) {
                // Check if this user conversation already exists
                UserConversation existingConvo = (UserConversation) userConversationsRepository.getUserConversationsByUserAndConversationId(
                        sampleUser.getId(), englishWeather.getId()).thenApply(userConversations -> {
                            if(userConversations.iterator().hasNext()){
                                return (UserConversation) userConversations.iterator().next();
                            }
                            else return null;
                }).join();

                if (existingConvo == null) {
                    // Create a new in-progress conversation
                    UserConversation userConvo = new UserConversation(
                            sampleUser.getId(), englishWeather.getId(), ConversationStatus.IN_PROGRESS.name());
                    userConversationsRepository.addUserConversation(userConvo);

                    // Add a few messages
                    messageRepository.addMessage(new Message(0, userConvo.getId(),
                            "Hello! How's the weather today?", "AI"));
                    messageRepository.addMessage(new Message(1, userConvo.getId(),
                            "It's really sunny and warm today. I like this weather.", "USER"));
                    messageRepository.addMessage(new Message(2, userConvo.getId(),
                            "That sounds lovely! What's your favorite season of the year?", "AI"));
                    messageRepository.addMessage(new Message(3, userConvo.getId(),
                            "I think I like summer the best because I can go swimming.", "USER"));
                    messageRepository.addMessage(new Message(4, userConvo.getId(),
                            "Summer is great for outdoor activities! Do you enjoy other activities in summer too?", "AI"));
                }
            }

            // 3. Not started Spanish shopping conversation
            Conversation spanishShopping = null;
            for (Conversation conv : conversations) {
                if (conv.getTitle() != null && conv.getTitle().contains("Shopping") &&
                        conv.getLanguage() != null && conv.getLanguage().getName().equals("Spanish")) {
                    spanishShopping = conv;
                    break;
                }
            }

            if (spanishShopping != null) {
                // Check if this user conversation already exists
                UserConversation existingConvo = (UserConversation) userConversationsRepository.getUserConversationsByUserAndConversationId(
                        sampleUser.getId(), spanishShopping.getId()).thenApply(userConversations -> {
                            if(userConversations.iterator().hasNext()){
                                return (UserConversation) userConversations.iterator().next();
                            }
                            else return null;
                }).join();

                if (existingConvo == null) {
                    // Create a new not started conversation
                    UserConversation userConvo = new UserConversation(
                            sampleUser.getId(), spanishShopping.getId(), ConversationStatus.NOTSTARTED.name());
                    userConversationsRepository.addUserConversation(userConvo);
                }
            }

            // 4. Completed Japanese food conversation
            Conversation japaneseFood = null;
            for (Conversation conv : conversations) {
                if (conv.getTitle() != null && conv.getTitle().equals("Talking About Food") &&
                        conv.getLanguage() != null && conv.getLanguage().getName().equals("Japanese")) {
                    japaneseFood = conv;
                    break;
                }
            }

            if (japaneseFood != null) {
                // Check if this user conversation already exists
                UserConversation existingConvo = (UserConversation) userConversationsRepository.getUserConversationsByUserAndConversationId(
                        sampleUser.getId(), japaneseFood.getId()).thenApply(userConversations -> {
                            if(userConversations.iterator().hasNext()){
                                return (UserConversation) userConversations.iterator().next();
                            }
                            else return null;
                }).join();

                if (existingConvo == null) {
                    // Create a new completed conversation
                    UserConversation userConvo = new UserConversation(
                            sampleUser.getId(), japaneseFood.getId(), ConversationStatus.COMPLETED.name());
                    userConversationsRepository.addUserConversation(userConvo);

                    // Add messages
                    messageRepository.addMessage(new Message(0, userConvo.getId(),
                            "こんにちは! 好きな食べ物は何ですか?", "AI"));  // Hello! What food do you like?
                    messageRepository.addMessage(new Message(1, userConvo.getId(),
                            "こんにちは! 寿司が好きです。", "USER"));  // Hello! I like sushi.
                    messageRepository.addMessage(new Message(2, userConvo.getId(),
                            "寿司はいいですね! どんな寿司が好きですか?", "AI"));  // Sushi is nice! What kind of sushi do you like?
                    messageRepository.addMessage(new Message(3, userConvo.getId(),
                            "マグロとサーモンが好きです。納豆は好きじゃないです。", "USER"));  // I like tuna and salmon. I don't like natto.
                    messageRepository.addMessage(new Message(4, userConvo.getId(),
                            "私もマグロとサーモンが好きです。他に好きな日本食はありますか?", "AI"));  // I also like tuna and salmon. Do you like other Japanese foods?
                    messageRepository.addMessage(new Message(5, userConvo.getId(),
                            "はい、ラーメンも好きです。特に味噌ラーメンが好きです。", "USER"));  // Yes, I also like ramen. Especially miso ramen.
                    messageRepository.addMessage(new Message(6, userConvo.getId(),
                            "味噌ラーメンは美味しいですね。日本に行ったことがありますか?", "AI"));  // Miso ramen is delicious. Have you been to Japan?
                    messageRepository.addMessage(new Message(7, userConvo.getId(),
                            "いいえ、まだです。でも、いつか行きたいです。", "USER"));  // No, not yet. But I want to go someday.

                    // Add an evaluation
                    Evaluation eval = new Evaluation(userConvo.getId(), 75, 100, 4, 3,
                            "Good vocabulary about food. Work on using more particles correctly and practice more complex sentence structures.");
                    evaluationRepository.addEvaluation(eval);
                }
            }

            // Add user languages
            IUserLanguageRepository userLanguageRepository = RepositoryFactory.getUserLanguageRepository();

            // French - Beginner
            Language french = languageRepository.getLanguageByName("French").join().orElse(null);
            LanguageLevel beginnerLevel = languageRepository.getLanguageLevelByName("A2").join().orElse(null);
            LanguageLevel beginnerLevelJpn = languageRepository.getLanguageLevelByName("N5").join().orElse(null);
            if (french != null && beginnerLevel != null) {
                UserLanguage existingLang = userLanguageRepository.getUserLanguageByLanguageIdAndUserId(french.getId(), sampleUser.getId()).join().orElse(null);
                if (existingLang == null) {
                    UserLanguage userFrench = new UserLanguage(sampleUser.getId(), french.getId(), beginnerLevel.getId(), 250);
                    userLanguageRepository.addUserLanguage(userFrench);
                }
            }

            // English - Advanced
            Language english = languageRepository.getLanguageByName("English").join().orElse(null);
            LanguageLevel advancedLevel = languageRepository.getLanguageLevelByName("C2").join().orElse(null);
            if (english != null && advancedLevel != null) {
                UserLanguage existingLang = userLanguageRepository.getUserLanguageByLanguageIdAndUserId(english.getId(), sampleUser.getId()).join().orElse(null);
                if (existingLang == null) {
                    UserLanguage userEnglish = new UserLanguage(sampleUser.getId(), english.getId(), advancedLevel.getId(), 750);
                    userLanguageRepository.addUserLanguage(userEnglish);
                }
            }

            // Japanese - Beginner
            Language japanese = languageRepository.getLanguageByName("Japanese").join().orElse(null);
            if (japanese != null && beginnerLevel != null) {
                UserLanguage existingLang = userLanguageRepository.getUserLanguageByLanguageIdAndUserId(japanese.getId(), sampleUser.getId()).join().orElse(null);
                if (existingLang == null) {
                    UserLanguage userJapanese = new UserLanguage(sampleUser.getId(), japanese.getId(), beginnerLevelJpn.getId(), 150);
                    userLanguageRepository.addUserLanguage(userJapanese);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Initialize default languages, language_systems and language_levels
     */
    private void initializeLanguages() throws SQLException {
        try{
            // Add language systems
            LanguageLevelSystem european = languageRepository.getLanguageLevelSystemByName("CEFR").join().orElse(null);
            LanguageLevelSystem japan = languageRepository.getLanguageLevelSystemByName("JLPT").join().orElse(null);

            //if(!languageRepository.getAllLanguageSystems().join().iterator().hasNext()){
                /**
                 * CEFR : Europe
                 */
                european = new LanguageLevelSystem("CEFR", "The Common European Framework Reference for Languages scale measures language abilities in six levels.");
                languageRepository.addLanguageSystem(european).join();

                LanguageLevel A1 = new LanguageLevel(european.getId(), "A1", 1);
                LanguageLevel A2 = new LanguageLevel(european.getId(), "A2", 2);
                LanguageLevel B1 = new LanguageLevel(european.getId(), "B1", 3);
                LanguageLevel B2 = new LanguageLevel(european.getId(), "B2", 4);
                LanguageLevel C1 = new LanguageLevel(european.getId(), "C1", 5);
                LanguageLevel C2 = new LanguageLevel(european.getId(), "C2", 6);

                languageRepository.addLanguageLevel(A1);
                languageRepository.addLanguageLevel(A2);
                languageRepository.addLanguageLevel(B1);
                languageRepository.addLanguageLevel(B2);
                languageRepository.addLanguageLevel(C1);
                languageRepository.addLanguageLevel(C2);

                /**
                 * JLPT : japan
                 */
                japan = new LanguageLevelSystem("JLPT", "Gets used to grade the level of the Japanese language");
                languageRepository.addLanguageSystem(japan).join();

                LanguageLevel N5 = new LanguageLevel(japan.getId(), "N5", 1);
                LanguageLevel N4 = new LanguageLevel(japan.getId(), "N4", 2);
                LanguageLevel N3 = new LanguageLevel(japan.getId(), "N3", 3);
                LanguageLevel N2 = new LanguageLevel(japan.getId(), "N2", 4);
                LanguageLevel N1 = new LanguageLevel(japan.getId(), "N1", 5);

                languageRepository.addLanguageLevel(N5);
                languageRepository.addLanguageLevel(N4);
                languageRepository.addLanguageLevel(N3);
                languageRepository.addLanguageLevel(N2);
                languageRepository.addLanguageLevel(N1);
            //}


            // Add languages
            Language french = new Language(european.getId(), "French", "fr", Color.BLUE.getValue(), 10000);
            languageRepository.addLanguage(french);

            Language english = new Language(european.getId(), "English", "en", Color.GREEN.getValue(), 10000);
            languageRepository.addLanguage(english);

            Language japanese = new Language(japan.getId(), "Japanese", "ja", Color.LIGHTBLUE.getValue(), 10000);
            languageRepository.addLanguage(japanese);

            Language spanish = new Language(european.getId(), "Spanish", "es", Color.RED.getValue(), 10000);
            languageRepository.addLanguage(spanish);

            Language german = new Language(european.getId(), "German","de", Color.ORANGE.getValue(), 10000);
            languageRepository.addLanguage(german);

            Language dutch = new Language(european.getId(), "Dutch", "nl", "#33ccff", 10000);
            languageRepository.addLanguage(dutch);


            //System.out.println("Adding mandarin");
            Language mandarin = new Language(european.getId(), "Mandarin", "zh", Color.YELLOW.getValue(), 10000);
            languageRepository.addLanguage(mandarin);

            Language malaise = new Language(european.getId(), "Malaise", "ms", "#1BF834", 10000);
            languageRepository.addLanguage(malaise);

            // Language levels are initialized by DatabaseManager
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Initialize default scenarios
     */
    private void initializeScenarios() throws SQLException {
        try{
            // Restaurant scenario
            Scenario restaurantScenario = new Scenario(
                    "You are at a restaurant in Paris and need to order food. " +
                            "Practice asking about menu items, making special requests, and paying the bill.",
                    Roles.WAITRESS.name()
            );
            restaurantScenario.setKeyPoints(Arrays.asList(
                    "Asking for recommendations",
                    "Ordering food and drinks",
                    "Making special requests (allergies, preferences)",
                    "Asking for the bill"
            ));
            scenarioRepository.addScenario(restaurantScenario);

            // Finding your way scenario
            Scenario findingWayScenario = new Scenario(
                    "You are lost in London and very hungry. Ask if there is anything to eat nearby to civilians around you.",
                    Roles.CIVILIAN.name()
            );
            findingWayScenario.setKeyPoints(Arrays.asList(
                    "Asking for directions",
                    "Talking about food",
                    "Starting a conversation with someone",
                    "Ending a conversation with someone"
            ));
            scenarioRepository.addScenario(findingWayScenario);

            // Food scenario
            Scenario foodScenario = new Scenario(
                    "You are talking to a friend about the foods you like or dislike.",
                    Roles.FRIEND.name()
            );
            foodScenario.setKeyPoints(Arrays.asList(
                    "Talking about food preferences",
                    "Discussing cooking",
                    "Comparing cuisines",
                    "Describing tastes and flavors"
            ));
            scenarioRepository.addScenario(foodScenario);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Initialize default conversations
     */
    private void initializeConversations() throws SQLException {
        try{
            System.out.println("Initializing conversations...");

            // Get languages from database - this is still needed
            Language french = languageRepository.getLanguageByName("French").join().orElse(null);
            Language english = languageRepository.getLanguageByName("English").join().orElse(null);
            Language japanese = languageRepository.getLanguageByName("Japanese").join().orElse(null);
            Language spanish = languageRepository.getLanguageByName("Spanish").join().orElse(null);
            Language german = languageRepository.getLanguageByName("German").join().orElse(null);

            // Get language systems
            LanguageLevelSystem european = languageRepository.getLanguageLevelSystemByName("CEFR").join().orElse(null);
            LanguageLevelSystem japan = languageRepository.getLanguageLevelSystemByName("JLPT").join().orElse(null);
            // Get language levels from database - this is still needed
            LanguageLevel beginner = european.getLevels().stream().filter(l -> l.getName().equals("A1")).findFirst().orElse(null);
            LanguageLevel beginnerJpn = japan.getLevels().stream().filter(l -> l.getName().equals("N5")).findFirst().orElse(null);
            LanguageLevel intermediate = european.getLevels().stream().filter(l -> l.getName().equals("B2")).findFirst().orElse(null);
            LanguageLevel intermediateJpn = japan.getLevels().stream().filter(l -> l.getName().equals("N4")).findFirst().orElse(null);
            LanguageLevel advanced = european.getLevels().stream().filter(l -> l.getName().equals("C2")).findFirst().orElse(null);
            LanguageLevel advancedJpn = japan.getLevels().stream().filter(l -> l.getName().equals("N2")).findFirst().orElse(null);

            // Create all scenarios directly in this method

            // Restaurant scenario
            Scenario restaurantScenario = new Scenario(
                    "You are at a restaurant and need to order food. " +
                            "Practice asking about menu items, making special requests, and paying the bill.",
                    Roles.WAITRESS.name()
            );
            restaurantScenario.setKeyPoints(Arrays.asList(
                    "Asking for recommendations",
                    "Ordering food and drinks",
                    "Making special requests (allergies, preferences)",
                    "Asking for the bill"
            ));
            scenarioRepository.addScenario(restaurantScenario);

            // Directions scenario
            Scenario directionsScenario = new Scenario(
                    "You are lost in a city and need to find your way. Ask people around you for directions to important landmarks or your destination.",
                    Roles.CIVILIAN.name()
            );
            directionsScenario.setKeyPoints(Arrays.asList(
                    "Asking for directions",
                    "Understanding spatial instructions",
                    "Confirming your understanding",
                    "Thanking someone for their help"
            ));
            scenarioRepository.addScenario(directionsScenario);

            // Food discussion scenario
            Scenario foodScenario = new Scenario(
                    "You're talking with a friend about foods you like and dislike, favorite restaurants, and cooking experiences.",
                    Roles.FRIEND.name()
            );
            foodScenario.setKeyPoints(Arrays.asList(
                    "Expressing food preferences",
                    "Discussing cooking and recipes",
                    "Comparing different cuisines",
                    "Describing tastes and flavors"
            ));
            scenarioRepository.addScenario(foodScenario);

            // Introduction scenario
            Scenario introductionScenario = new Scenario(
                    "You are meeting someone for the first time. Practice introducing yourself, talking about your hobbies, family, and asking questions to get to know the other person.",
                    Roles.FRIEND.name()
            );
            introductionScenario.setKeyPoints(Arrays.asList(
                    "Introducing yourself",
                    "Talking about hobbies and interests",
                    "Describing your family",
                    "Asking personal questions politely"
            ));
            scenarioRepository.addScenario(introductionScenario);

            // Shopping scenario
            Scenario shoppingScenario = new Scenario(
                    "You are at a clothing store and want to buy some new clothes. Practice asking about sizes, colors, prices, and making a purchase.",
                    Roles.CIVILIAN.name()
            );
            shoppingScenario.setKeyPoints(Arrays.asList(
                    "Asking about available sizes",
                    "Describing colors and styles you like",
                    "Inquiring about prices and discounts",
                    "Making a purchase and payment"
            ));
            scenarioRepository.addScenario(shoppingScenario);

            // Doctor scenario
            Scenario doctorScenario = new Scenario(
                    "You're not feeling well and are visiting a doctor. Describe your symptoms, answer their questions, and understand their recommendations.",
                    Roles.DOCTOR.name()
            );
            doctorScenario.setKeyPoints(Arrays.asList(
                    "Describing symptoms and pain",
                    "Answering medical history questions",
                    "Understanding diagnosis and treatment",
                    "Asking questions about medications"
            ));
            scenarioRepository.addScenario(doctorScenario);

            // Weather scenario
            Scenario weatherScenario = new Scenario(
                    "You're chatting with a friend about the weather and your plans for different weather conditions. Discuss seasons, weather forecasts, and activities.",
                    Roles.FRIEND.name()
            );
            weatherScenario.setKeyPoints(Arrays.asList(
                    "Describing current weather conditions",
                    "Talking about favorite seasons",
                    "Planning activities for different weather",
                    "Discussing weather forecasts and climate"
            ));
            scenarioRepository.addScenario(weatherScenario);

            // Now create all conversations using the scenarios we just created

            // FRENCH CONVERSATIONS

            // 1. Restaurant ordering in French (Beginner)
            Conversation frenchRestaurant = new Conversation(
                    "Restaurant Ordering",
                    "Practice ordering food and drinks in a restaurant setting",
                    french.getId(),
                    beginner.getId(),
                    restaurantScenario.getId(),
                    "You are a waitress at a restaurant in Paris. The user will try to order something. Speak in French with the user, but keep the language simple since they are a beginner.",
                    AIModels.CONVERSATION.getModel()
            );
            frenchRestaurant.setLanguageFrom(english);
            conversationRepository.addConversation(frenchRestaurant);

            // 2. Shopping in French (Beginner)
            Conversation frenchShopping = new Conversation(
                    "Shopping for Clothes",
                    "Practice shopping for clothes and accessories in French",
                    french.getId(),
                    beginner.getId(),
                    shoppingScenario.getId(),
                    "You are a clothing store salesperson in France. Help the customer find what they're looking for. Speak in simple French appropriate for beginners.",
                    AIModels.CONVERSATION.getModel()
            );
            frenchShopping.setLanguageFrom(english);
            conversationRepository.addConversation(frenchShopping);

            // 3. Introductions in French (Intermediate)
            Conversation frenchIntroduction = new Conversation(
                    "Meeting New People",
                    "Practice introducing yourself and making small talk in French",
                    french.getId(),
                    intermediate.getId(),
                    introductionScenario.getId(),
                    "You are a French person at a social gathering. You've just met the user and want to get to know them. Speak in French at an intermediate level.",
                    AIModels.CONVERSATION.getModel()
            );
            frenchIntroduction.setLanguageFrom(english);
            conversationRepository.addConversation(frenchIntroduction);

            // ENGLISH CONVERSATIONS

            // 1. Finding your way in English (Advanced)
            Conversation englishDirections = new Conversation(
                    "Finding Your Way",
                    "Practice asking for directions in a city setting",
                    english.getId(),
                    advanced.getId(),
                    directionsScenario.getId(),
                    "You are a local in London. You see someone looking lost and ask if they need help. Use advanced English vocabulary and expressions.",
                    AIModels.CONVERSATION.getModel()
            );
            englishDirections.setLanguageFrom(english);
            conversationRepository.addConversation(englishDirections);

            // 2. Doctor visit in English (Intermediate)
            Conversation englishDoctor = new Conversation(
                    "Doctor's Appointment",
                    "Practice explaining health issues and understanding medical advice",
                    english.getId(),
                    intermediate.getId(),
                    doctorScenario.getId(),
                    "You are a doctor in an English-speaking clinic. Your patient (the user) isn't feeling well. Use medical terms but explain them clearly for an intermediate English speaker.",
                    AIModels.CONVERSATION.getModel()
            );
            englishDoctor.setLanguageFrom(english);
            conversationRepository.addConversation(englishDoctor);

            // 3. Weather talk in English (Beginner)
            Conversation englishWeather = new Conversation(
                    "Talking About Weather",
                    "Practice discussing weather, seasons, and related activities",
                    english.getId(),
                    beginner.getId(),
                    weatherScenario.getId(),
                    "You are talking with a friend about the weather. Use simple English vocabulary and phrases about weather, seasons, and activities.",
                    AIModels.CONVERSATION.getModel()
            );
            englishWeather.setLanguageFrom(english);
            conversationRepository.addConversation(englishWeather);

            // JAPANESE CONVERSATIONS

            // 1. Talking about food in Japanese (Beginner)
            Conversation japaneseFood = new Conversation(
                    "Talking About Food",
                    "Practice discussing foods you like and dislike in Japanese",
                    japanese.getId(),
                    beginnerJpn.getId(),
                    foodScenario.getId(),
                    "You are a friend of the user. Try asking them what their favorite foods are. Use simple Japanese suitable for beginners.",
                    AIModels.CONVERSATION.getModel()
            );
            japaneseFood.setLanguageFrom(english);
            conversationRepository.addConversation(japaneseFood);

            // 2. Shopping in Japanese (Beginner)
            Conversation japaneseShopping = new Conversation(
                    "Shopping in Japan",
                    "Practice shopping for items in Japanese stores",
                    japanese.getId(),
                    beginnerJpn.getId(),
                    shoppingScenario.getId(),
                    "You are a store clerk in Japan. Help the customer find what they need. Use simple Japanese and focus on numbers, sizes, and colors.",
                    AIModels.CONVERSATION.getModel()
            );
            japaneseShopping.setLanguageFrom(english);
            conversationRepository.addConversation(japaneseShopping);

            // SPANISH CONVERSATIONS

            // 1. Restaurant in Spanish (Beginner)
            Conversation spanishRestaurant = new Conversation(
                    "Ordering in Spanish",
                    "Practice ordering food and drinks at a Spanish restaurant",
                    spanish.getId(),
                    beginner.getId(),
                    restaurantScenario.getId(),
                    "You are a waiter/waitress at a restaurant in Madrid. The user will order food and drinks. Use simple Spanish phrases and be patient with beginners.",
                    AIModels.CONVERSATION.getModel()
            );
            spanishRestaurant.setLanguageFrom(english);
            conversationRepository.addConversation(spanishRestaurant);

            // 2. Introductions in Spanish (Intermediate)
            Conversation spanishIntroduction = new Conversation(
                    "Meeting People in Spanish",
                    "Practice introducing yourself and making conversation in Spanish",
                    spanish.getId(),
                    intermediate.getId(),
                    introductionScenario.getId(),
                    "You are meeting the user at a party in Spain. Ask about their life, hobbies, and interests. Use intermediate-level Spanish vocabulary and expressions.",
                    AIModels.CONVERSATION.getModel()
            );
            spanishIntroduction.setLanguageFrom(english);
            conversationRepository.addConversation(spanishIntroduction);

            // GERMAN CONVERSATIONS

            // 1. Shopping in German (Beginner)
            Conversation germanShopping = new Conversation(
                    "Shopping in German",
                    "Practice common phrases for shopping in German",
                    german.getId(),
                    beginner.getId(),
                    shoppingScenario.getId(),
                    "You are a sales assistant in a German clothing store. Help the customer find what they need using simple German phrases.",
                    AIModels.CONVERSATION.getModel()
            );
            germanShopping.setLanguageFrom(english);
            conversationRepository.addConversation(germanShopping);

            // 2. Weather talk in German (Intermediate)
            Conversation germanWeather = new Conversation(
                    "Weather in German",
                    "Practice discussing weather, seasons and activities in German",
                    german.getId(),
                    intermediate.getId(),
                    weatherScenario.getId(),
                    "You are chatting with the user about the weather in Germany. Use intermediate German vocabulary about weather, seasons, and outdoor activities.",
                    AIModels.CONVERSATION.getModel()
            );
            germanWeather.setLanguageFrom(english);
            conversationRepository.addConversation(germanWeather);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Reset all application data (DANGEROUS!)
     */
    public void resetAllData() throws SQLException {
        // This method would delete all data from all tables
        // Use with extreme caution!
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Delete from tables in reverse order of foreign key dependencies
            conn.createStatement().executeUpdate("DELETE FROM evaluations");
            conn.createStatement().executeUpdate("DELETE FROM messages");
            conn.createStatement().executeUpdate("DELETE FROM user_conversations");
            conn.createStatement().executeUpdate("DELETE FROM conversations");
            conn.createStatement().executeUpdate("DELETE FROM scenario_key_points");
            conn.createStatement().executeUpdate("DELETE FROM scenarios");
            conn.createStatement().executeUpdate("DELETE FROM user_languages");
            conn.createStatement().executeUpdate("DELETE FROM languages");
            conn.createStatement().executeUpdate("DELETE FROM language_levels");
            conn.createStatement().executeUpdate("DELETE FROM language_systems");
            conn.createStatement().executeUpdate("DELETE FROM user_stats");
            conn.createStatement().executeUpdate("DELETE FROM users");

            conn.commit();

            // Reinitialize default data
            initializeDefaultData();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
