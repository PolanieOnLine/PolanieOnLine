package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.json.simple.JSONValue;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/** Read-only administrator inspection using the client's standard Swing delegates. */
public final class AdminInspectWindow extends JDialog {
    private static final long serialVersionUID=1L;
    private static AdminInspectWindow instance;
    private final JPanel navigation=new DialogPanel(null);
    private final JLabel caption=new JLabel(), count=new JLabel();
    private final JTextField search=new JTextField();
    private final JTextArea details=new JTextArea();
    private final DefaultTableModel model=new DefaultTableModel() {
        @Override public boolean isCellEditable(int row,int column) { return false; }
    };
    private final JTable table=new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter=new TableRowSorter<>(model);
    private final List<Map<String,Object>> sections=new ArrayList<>();
    private final List<JButton> categories=new ArrayList<>();
    private String selected="", windowId="", request="";
    private List<?> currentDetails=new ArrayList<Object>();

    private AdminInspectWindow(final Frame parent) {
        super(parent,"Inspect",false);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content=new JPanel(new BorderLayout(10,8));
        content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));content.setPreferredSize(new Dimension(820,510));
        navigation.setLayout(new BoxLayout(navigation,BoxLayout.Y_AXIS));navigation.setPreferredSize(new Dimension(150,0));
        content.add(navigation,BorderLayout.WEST);
        JPanel body=new DialogPanel(new BorderLayout(0,7));
        caption.putClientProperty("html.disable",Boolean.TRUE);body.add(caption,BorderLayout.NORTH);
        table.setRowSorter(sorter);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(Math.max(23,new JLabel().getFont().getSize()+9));table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer() {
            { putClientProperty("html.disable",Boolean.TRUE); }
        });
        details.setEditable(false);details.setLineWrap(true);details.setWrapStyleWord(true);details.setFont(new JLabel().getFont());
        details.setOpaque(false);details.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        Style style=StyleUtil.getStyle();if(style!=null){details.setForeground(style.getForeground());details.setBackground(style.getPlainColor());details.setOpaque(true);}
        JScrollPane tableScroll=new JScrollPane(table),detailScroll=new JScrollPane(details);
        detailScroll.setPreferredSize(new Dimension(400,120));detailScroll.setOpaque(false);detailScroll.getViewport().setOpaque(false);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,tableScroll,detailScroll);split.setResizeWeight(0.75);split.setBorder(BorderFactory.createEmptyBorder());
        body.add(split,BorderLayout.CENTER);
        JPanel filter=new DialogPanel(new BorderLayout(6,0));filter.add(new JLabel("Szukaj:"),BorderLayout.WEST);filter.add(search,BorderLayout.CENTER);filter.add(count,BorderLayout.EAST);
        body.add(filter,BorderLayout.SOUTH);content.add(body,BorderLayout.CENTER);
        JPanel footer=new DialogPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        JButton copy=new JButton("Kopiuj wiersz"),refresh=new JButton("Odśwież"),close=new JButton("Zamknij");
        copy.addActionListener(e->{if(table.getSelectedRow()>=0)Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(details.getText()),null);});
        refresh.addActionListener(e->refresh());close.addActionListener(e->dispose());footer.add(copy);footer.add(refresh);footer.add(close);content.add(footer,BorderLayout.SOUTH);
        setContentPane(content);pack();setMinimumSize(new Dimension(680,420));setLocationRelativeTo(parent);
        WindowUtils.closeOnEscape(this);WindowUtils.watchFontSize(this);WindowUtils.trackLocation(this,"admin-inspect",false);
        addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){if(instance==AdminInspectWindow.this)instance=null;}});
        search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){filter();}public void removeUpdate(DocumentEvent e){filter();}public void changedUpdate(DocumentEvent e){filter();}});
        table.getSelectionModel().addListSelectionListener(e->showDetails());
    }
    public static void show(final RPEvent event) {
        SwingUtilities.invokeLater(()->{
            boolean refresh="refresh".equals(event.get("phase"));
            if(refresh&&(instance==null||!instance.isDisplayable()||!instance.windowId.equals(event.get("window_id"))))return;
            if(instance==null||!instance.isDisplayable())instance=new AdminInspectWindow(j2DClient.get().getMainFrame());
            instance.apply(event);instance.setVisible(true);if(!refresh)instance.toFront();
        });
    }
    public static void closeOnLogout() {
        SwingUtilities.invokeLater(()->{AdminInspectWindow old=instance;instance=null;if(old!=null)old.dispose();});
    }
    @SuppressWarnings("unchecked")
    private void apply(final RPEvent event) {
        Object parsed=JSONValue.parse(event.get("payload"));if(!(parsed instanceof Map))return;
        Map<?,?> snapshot=(Map<?,?>)parsed;
        if(!windowId.equals(event.get("window_id"))){selected="";search.setText("");}
        windowId=event.get("window_id");request=event.get("request");setTitle("Inspect · "+String.valueOf(snapshot.get("title")));
        sections.clear();categories.clear();navigation.removeAll();
        Object raw=snapshot.get("sections");if(raw instanceof List)for(Object section:(List<?>)raw)if(section instanceof Map)sections.add((Map<String,Object>)section);
        for(Map<String,Object> section:sections){JButton button=new JButton(String.valueOf(section.get("title")));button.setHorizontalAlignment(SwingConstants.LEFT);button.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));button.addActionListener(e->select(String.valueOf(section.get("id"))));categories.add(button);navigation.add(button);navigation.add(Box.createVerticalStrut(5));}
        if(sections.stream().noneMatch(section->selected.equals(section.get("id"))))selected=sections.isEmpty()?"":String.valueOf(sections.get(0).get("id"));
        select(selected);navigation.revalidate();navigation.repaint();
    }
    private void select(final String id) {
        selected=id;table.clearSelection();details.setText("");
        for(int i=0;i<sections.size();i++){
            Map<String,Object> section=sections.get(i);boolean active=id.equals(section.get("id"));JButton button=categories.get(i);
            button.setSelected(active);button.setFont(button.getFont().deriveFont(active?Font.BOLD:Font.PLAIN));
            if(active){Style style=StyleUtil.getStyle();if(style!=null)button.setBorder(BorderFactory.createCompoundBorder(style.getBorderDown(),BorderFactory.createEmptyBorder(1,2,1,2)));}
            else button.updateUI();
            if(active){caption.setText(String.valueOf(section.get("title")));List<?> columns=(List<?>)section.get("columns"),rows=(List<?>)section.get("rows");
                currentDetails=(List<?>)section.get("details");Object[][] data=new Object[rows.size()][columns.size()];
                for(int r=0;r<rows.size();r++){List<?> row=(List<?>)rows.get(r);for(int c=0;c<columns.size();c++)data[r][c]=c<row.size()?row.get(c):"";}
                model.setDataVector(data,columns.toArray());}
        }
        filter();
    }
    private void filter(){table.clearSelection();String value=search.getText().trim();sorter.setRowFilter(value.isEmpty()?null:RowFilter.regexFilter("(?iu)"+Pattern.quote(value)));count.setText(table.getRowCount()+" / "+model.getRowCount());}
    private void showDetails(){int row=table.getSelectedRow();if(row<0){details.setText("");return;}row=table.convertRowIndexToModel(row);
        String value=row<currentDetails.size()?String.valueOf(currentDetails.get(row)):"";
        if(value.isEmpty()){StringBuilder text=new StringBuilder();for(int c=0;c<model.getColumnCount();c++)text.append(model.getColumnName(c)).append(": ").append(model.getValueAt(row,c)).append("\n");value=text.toString();}
        details.setText(value);details.setCaretPosition(0);
    }
    @SuppressWarnings("unchecked") private void refresh(){Object parsed=JSONValue.parse(request);if(!(parsed instanceof Map))return;RPAction action=new RPAction();
        for(Map.Entry<?,?> field:((Map<?,?>)parsed).entrySet()){if(field.getValue() instanceof List)action.put(String.valueOf(field.getKey()),(List<String>)field.getValue());else action.put(String.valueOf(field.getKey()),String.valueOf(field.getValue()));}
        action.put("inspection_window",windowId);StendhalClient.get().send(action);
    }
}
