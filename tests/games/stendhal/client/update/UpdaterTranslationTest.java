package games.stendhal.client.update;

import static org.junit.Assert.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/** Tests the bootstrap UI without downloading, deleting or installing anything. */
public class UpdaterTranslationTest {
 @Rule public TemporaryFolder temporary = new TemporaryFolder();
 private String originalHome;
 @Before public void isolatedEnglishProfile() throws Exception {
  originalHome=System.getProperty("user.home");
  System.setProperty("user.home",temporary.getRoot().getAbsolutePath());
  saveLanguage("en");
 }
 @After public void restoreHome() {System.setProperty("user.home",originalHome);}
 private void saveLanguage(String language) throws Exception {
  File directory=new File(temporary.getRoot(),"polanieonline"); directory.mkdirs();
  // Mirrors the existing client folder convention on Unix systems too.
  File profile=new File(directory,"user.dat"); profile.createNewFile();
  Properties properties=new Properties(); properties.setProperty("config.ui.language",language);
  try(FileOutputStream output=new FileOutputStream(new File(directory,"windows.properties"))) {properties.store(output,"");}
 }
 @Test public void updateAndInstallPromptsUseTheSavedLanguageAndKeepDownloadSize() throws Exception {
  for(boolean update:new boolean[]{true,false}) {
   AtomicReference<String[]> result=new AtomicReference<String[]>();
   SwingUtilities.invokeAndWait(()->{capture(result);assertFalse(UpdateGUIDialogs.askForDownload(20480,update));});
   assertEquals("PolanieOnLine update",result.get()[0]);
   assertEquals(update?"A new version is available (20 KB).\r\nUpdate PolanieOnLine?":
    "Additional files must be downloaded (20 KB).\r\nInstall PolanieOnLine?",result.get()[1]);
   assertEquals("Yes",result.get()[2]); assertEquals("No",result.get()[3]);
  }
 }
 @Test public void updateMessageIsEnglishAndPolishModeRemainsPolish() throws Exception {
  AtomicReference<String[]> result=new AtomicReference<String[]>();
  SwingUtilities.invokeAndWait(()->{capture(result);UpdateGUIDialogs.messageBox("Wystąpił błąd podczas aktualizacji");});
  assertEquals("An error occurred during the update.",result.get()[1]);
  saveLanguage("pl");
  SwingUtilities.invokeAndWait(()->{capture(result);UpdateGUIDialogs.messageBox("Wystąpił błąd podczas aktualizacji");});
  assertEquals("Aktualizacja PolanieOnLine",result.get()[0]);
  assertEquals("Wystąpił błąd podczas aktualizacji",result.get()[1]);
 }
 @Test public void progressWindowUsesEnglishWithoutLoadingAnyRemotePage() throws Exception {
  SwingUtilities.invokeAndWait(()->{
   UpdateProgressBar progress=new UpdateProgressBar(100,null,"1.42","1.42");
   try {assertEquals("PolanieOnLine update",progress.getTitle());
    assertTrue(hasLabel(progress,"Downloading updates..."));}
   finally {progress.dispose();}
  });
 }
 @Test public void diagnosticsKeepPathsStackTracesAndPlaceholderLikeTextVerbatim() {
  String trace="java.lang.Exception: Witaj. {0} $FEE\r\n\tat test.run(Test.java:12)";
  String source="Zamknij. Wystąpił niespodziewany błąd.\r\nPrzejdź do formularza kontaktowego na https://polanieonline.eu/kontakt i napisz wiadomość o błędzie:\r\n"+trace;
  assertEquals("Zamknij. An unexpected error occurred.\r\nPlease report it using https://polanieonline.eu/kontakt and include these details:\r\n"+trace,UpdateLanguage.getText(source));
  assertEquals("An error occurred while downloading update file C:\\Zamknij\\{0}.jar",
   UpdateLanguage.getText("Podczas pobierania aktualizacji wystąpił błąd w pliku C:\\Zamknij\\{0}.jar"));
 }
 @Test public void unknownOrMissingSavedLanguageSafelyFallsBackToPolish() throws Exception {
  saveLanguage("es");assertEquals("Pobieram...",UpdateLanguage.getText("Pobieram..."));
  File preferences=new File(temporary.getRoot(),"polanieonline/windows.properties");
  assertTrue(preferences.delete());
  assertEquals("Pobieram...",UpdateLanguage.getText("Pobieram..."));
 }
 @Test public void translatedChoicesKeepYesNoCancelAndCloseReturnValues() throws Exception {
  int[][] cases={{JOptionPane.YES_NO_OPTION,0,JOptionPane.YES_OPTION},
   {JOptionPane.YES_NO_OPTION,1,JOptionPane.NO_OPTION},
   {JOptionPane.OK_CANCEL_OPTION,1,JOptionPane.CANCEL_OPTION},
   {JOptionPane.YES_NO_CANCEL_OPTION,2,JOptionPane.CANCEL_OPTION},
   {JOptionPane.YES_NO_OPTION,-1,JOptionPane.CLOSED_OPTION}};
  for(int[] row:cases) SwingUtilities.invokeAndWait(()->{
   capture(new AtomicReference<String[]>(),row[1]);
   assertEquals(row[2],LoneOptionDialog.showConfirmDialog("Czy chcesz przerwać pobieranie?","Potwierdzenie",row[0],JOptionPane.QUESTION_MESSAGE));
  });
 }
 @Test public void cancellingTheDownloadAsksInEnglishAndNoLeavesTheWindowOpen() throws Exception {
  AtomicReference<String[]> result=new AtomicReference<String[]>();
  SwingUtilities.invokeAndWait(()->{
   JFrame progress=new JFrame();progress.pack();
   try {capture(result);
    new UpdateProgressBarWindowListener().windowClosing(new java.awt.event.WindowEvent(progress,java.awt.event.WindowEvent.WINDOW_CLOSING));
    assertTrue(progress.isDisplayable());}
   finally {progress.dispose();}
  });
  assertEquals("Confirmation",result.get()[0]);assertEquals("Do you want to cancel the download?",result.get()[1]);
 }
 private boolean hasLabel(Container root,String text) {
  for(Component child:root.getComponents()) {
   if(child instanceof JLabel&&text.equals(((JLabel)child).getText()))return true;
   if(child instanceof Container&&hasLabel((Container)child,text))return true;
  } return false;
 }
 private void capture(AtomicReference<String[]> result) {
  capture(result,1);
 }
 private void capture(AtomicReference<String[]> result,int choice) {
  Timer timer=new Timer(20,null); final int[] attempts={0};
  timer.addActionListener(event->{
   for(Window window:Window.getWindows()) if(window instanceof JDialog&&window.isShowing()) {
    JOptionPane pane=findPane((Container)window);if(pane==null)continue;
    Object message=pane.getMessage();Object[] options=pane.getOptions();
    result.set(new String[]{((JDialog)window).getTitle(),message instanceof JTextArea?((JTextArea)message).getText():String.valueOf(message),
     options==null?"":String.valueOf(options[0]),options==null||options.length<2?"":String.valueOf(options[1])});
    timer.stop();if(choice>=0)pane.setValue(options==null?choice:options[Math.min(choice,options.length-1)]);window.dispose();return;
   }
   if(++attempts[0]>250)timer.stop();
  });timer.start();
 }
 private JOptionPane findPane(Container root) {
  for(Component child:root.getComponents()) {
   if(child instanceof JOptionPane)return (JOptionPane)child;
   if(child instanceof Container) {JOptionPane found=findPane((Container)child);if(found!=null)return found;}
  }return null;
 }
}
