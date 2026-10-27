package games.stendhal.client.update;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/** Reads the same saved language and catalogue format before the game jars are loaded.
 * Deliberately depends only on the JRE: the starter cannot load client GUI or logging classes.
 */
final class UpdateLanguage {
 private static final Map<String,String> ENGLISH = load();
 private static final Pattern SLOT = Pattern.compile("\\{(\\d+)\\}");
 private UpdateLanguage() { }

 static String getText(String source) {
  if(source==null || !"en".equals(savedLanguage()))return source;
  String exact=ENGLISH.get(source);if(exact!=null)return exact;
  for(Map.Entry<String,String> entry:ENGLISH.entrySet()) {
   Matcher slots=SLOT.matcher(entry.getKey());
   StringBuilder regex=new StringBuilder("^");int offset=0;
   Map<String,Integer> groups=new LinkedHashMap<String,Integer>();
   while(slots.find()) {
    regex.append(Pattern.quote(entry.getKey().substring(offset,slots.start())));
    Integer group=groups.get(slots.group(1));
    if(group==null) {groups.put(slots.group(1),groups.size()+1);regex.append("(.*?)");}
    else regex.append('\\').append(group);
    offset=slots.end();
   }
   if(groups.isEmpty())continue;
   regex.append(Pattern.quote(entry.getKey().substring(offset))).append('$');
   Matcher match=Pattern.compile(regex.toString(),Pattern.DOTALL).matcher(source);
   if(!match.matches())continue;
   Matcher output=SLOT.matcher(entry.getValue());StringBuffer result=new StringBuffer();
   while(output.find())output.appendReplacement(result,Matcher.quoteReplacement(match.group(groups.get(output.group(1)))));
   output.appendTail(result);return result.toString();
  }
  return source;
 }

 private static String savedLanguage() {
  try {
   File folder=new File(System.getProperty("user.home"),ClientGameConfiguration.get("GAME_NAME").toLowerCase(Locale.ROOT));
   if(System.getProperty("os.name","").matches("AIX|Digital Unix|FreeBSD|HP UX|Irix|Linux|Mac OS X|Solaris")
     && !new File(folder,"user.dat").exists())folder=new File(new File(System.getProperty("user.home"),".config"),folder.getName());
   Properties properties=new Properties();
   try(InputStream input=new FileInputStream(new File(folder,"windows.properties"))) {properties.load(input);}
   return properties.getProperty("config.ui.language","pl").trim().toLowerCase(Locale.ROOT);
  } catch(IOException | IllegalArgumentException | SecurityException error) {return "pl";}
 }

 private static Map<String,String> load() {
  Map<String,String> entries=new LinkedHashMap<String,String>();
  try(InputStream input=UpdateLanguage.class.getResourceAsStream("/data/languages/en-updater.txt")) {
   if(input==null)return entries;
   try(BufferedReader reader=new BufferedReader(new InputStreamReader(input,"UTF-8"))) {
    String line;while((line=reader.readLine())!=null) {
     if(line.isEmpty()||line.startsWith("#"))continue;
     int equals=line.indexOf('=');
     if(equals>0)entries.put(unescape(line.substring(0,equals)),unescape(line.substring(equals+1)));
    }
   }
  } catch(IOException error) { /* Missing translations must not prevent startup. */ }
  return entries;
 }
 private static String unescape(String text) {
  StringBuilder result=new StringBuilder();
  for(int i=0;i<text.length();i++) {
   char c=text.charAt(i);
   if(c=='\\'&&i+1<text.length()) {
    char escaped=text.charAt(++i);
    result.append(escaped=='n'?'\n':escaped=='r'?'\r':escaped=='t'?'\t':escaped);
   } else result.append(c);
  }return result.toString();
 }
}
