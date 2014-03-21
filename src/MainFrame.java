import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: 1
 * Date: 02.01.13
 * Time: 0:35
 * To change this template use File | Settings | File Templates.
 */
public class MainFrame extends JFrame {
    private static MainFrame instance;
    private JFileChooser chooser = new JFileChooser();
    private File[] tableFiles;

    private File outDirectory;
    private String outName = "Result";

    private TextField regexpField = new TextField("([ ]*)");
    private TextField separatorsField = new TextField(";\\t");
    private TextArea tableFilesTextArea = new TextArea("");
    private TextField directoryFileField = new TextField("");


    private JFrame frame;
    private final String CSV_EXT = "csv";
    private Components components = new Components();
    private static String title = "Table explorer v1.0";

    private MainFrame(String title) throws HeadlessException {
        super(title);
        SetSetting();
        setConfigs();
        setTextAreas();
    }

    public static void main(String[] args) {
        MainFrame frame = MainFrame.getInstance();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void setConfigs() {
        Config config = Config.getInstance();
        if (!config.isRead()) return;
        String srcDirPath = config.getSourceDirectoryPath();
        String outDirPath = config.getOutDirectoryPath();
        String outName = config.getOutName();
        String separators = config.getSeparators();
        if (srcDirPath != null) {
            File dir = new File(srcDirPath);
            if (dir.exists()) {
                tableFiles = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith("." + CSV_EXT);
                    }
                });
                updateTableFilesTextArea();
            }
        }
        if (outDirPath != null) {
            outDirectory = new File(outDirPath);
        }
        if (outName != null && !outName.equals("")) {
            this.outName = outName;
        }
        if (separators != null && !separators.equals("")) {
            separatorsField.setText(separators);
        }
    }

    private void setAreasButtonsOptions() {
        directoryFileField.setEditable(false);
        tableFilesTextArea.setEditable(false);
    }


    public static MainFrame getInstance() {
        if (instance == null) instance = new MainFrame(title);
        return instance;
    }

    private void SetSetting() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(700, 420);
        setLocation(size.width / 10, size.height / 10);
        setResizable(false);
        regexpField.setEditable(false);
        regexpField.setEnabled(false);
        frame = this;
    }

    /*
    *    @extension if it has nullable length, it means directory
    */
    private void setChooserOptions(final String extension) {
        chooser.setCurrentDirectory(new File("."));
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (extension.length() == 0) return f.isDirectory();
                return f.isDirectory() || f.getName().toLowerCase().endsWith("." + extension);
            }

            @Override
            public String getDescription() {
                if (extension.length() == 0) return "directory files";
                return extension + " files";
            }
        });
    }


    private void setTextAreas() {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        GridBagConstraints c = getGridBagConstraints();
        addComponent(gbl, c, 0, 1, components.ONLY_SPACE_FIELDS);

        addComponent(gbl, c, 1, 0, components.REGEXP_TEXT);
        addComponent(gbl, c, 1, 1, regexpField);

        addComponent(gbl, c, 2, 0, components.SEPARATORS_TEXT);
        addComponent(gbl, c, 2, 1, separatorsField);

        addComponent(gbl, c, 3, 0, components.TABLE_TEXT);
        addComponent(gbl, c, 3, 1, tableFilesTextArea);
        addComponent(gbl, c, 3, 2, components.tableButton);

        addComponent(gbl, c, 4, 0, components.DIRECTORY_TEXT);
        addComponent(gbl, c, 4, 1, directoryFileField);
        addComponent(gbl, c, 4, 2, components.directoryButton);
        c.fill = GridBagConstraints.NONE;
        addComponent(gbl, c, 5, 0, components.runButton);
        addComponent(gbl, c, 5, 1, components.aboutButton);
        repaintTextAreas();
        setAreasButtonsOptions();
    }

    private void repaintTextAreas() {
        updateTableFilesTextArea();
        updateDirectoryField();
    }

    private void addComponent(GridBagLayout gbl, GridBagConstraints c, int y, int x, Component component) {
        c.gridx = x;
        c.gridy = y;
        gbl.setConstraints(component, c);
        add(component);
    }


    private GridBagConstraints getGridBagConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.insets = new Insets(10, 5, 0, 5);
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 0.0;
        c.weighty = 0.0;
        return c;
    }


    private File addDirectoryFile() {
        File f = getSelectedSaveDirectoryFile();
        outDirectory = f;
        updateDirectoryField();
        return f;
    }

    private void addTablesFiles() {
        final String extension = CSV_EXT;
        chooser.setCurrentDirectory(new File("."));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith("." + extension);
            }

            @Override
            public String getDescription() {
                return extension + " files or directory(ies) of files";
            }
        });
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File[] files = chooser.getSelectedFiles();
        ArrayList<File> resFiles = new ArrayList<File>();
        for (File f : files) {
            if (f.isDirectory()) {
                Collections.addAll(resFiles, f.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith("." + CSV_EXT);
                    }
                }));
            } else {
                resFiles.add(f);
            }
        }
        tableFiles = new File[(resFiles).size()];
        for (int i = 0; i < (resFiles).size(); i++) {
            tableFiles[i] = resFiles.get(i);
        }
        updateTableFilesTextArea();
    }

    private void updateDirectoryField() {
        if (outDirectory != null && !(outDirectory.getPath()).equals("")) {
            directoryFileField.setText(outDirectory.getAbsolutePath());
        } else {
            directoryFileField.setText("");
        }
        directoryFileField.repaint();
    }

    private void updateTableFilesTextArea() {
        if (tableFiles != null && tableFiles.length > 0) {
            StringBuilder str = new StringBuilder("");
            for (int i = 0; i < tableFiles.length; i++) {
                if (i != 0) str.append("\n");
                String s = tableFiles[i].getPath();
                str.append(s);
            }
            tableFilesTextArea.setText(str.toString());
        } else tableFilesTextArea.setText("");
        tableFilesTextArea.repaint();
    }


    private File getSelectedSaveDirectoryFile() {
        setChooserOptions("");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        return chooser.getSelectedFile();
    }

    private void startSearch() {
        if (tableFiles == null && outDirectory == null) {
            JOptionPane.showMessageDialog(this, "Файлы таблиц или директория выхода не выбраны", "Ошибка", JOptionPane.OK_OPTION);
            return;
        }
        TableExplorer explorer = new TableExplorer(tableFiles, outDirectory, outName, regexpField.getText(), separatorsField.getText());
        explorer.createOutTable();
        String message = "Таблицы исследованы, результаты записаны в таблицу. Открыть папку с таблицей?";
        int response = JOptionPane.showConfirmDialog(this, message, "Success", JOptionPane.OK_CANCEL_OPTION);
        if (response == JOptionPane.OK_OPTION) {
            try {
                Runtime.getRuntime().exec("explorer.exe " + outDirectory.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка при открытии директории выхода", "Ошибка", JOptionPane.OK_OPTION);
            }
        }
    }

    class Components {
        public JRadioButton ONLY_SPACE_FIELDS = new JRadioButton("Искать только пустые поля", true);
        public JLabel REGEXP_TEXT = new JLabel("Регулярное выражение");
        public JLabel SEPARATORS_TEXT = new JLabel("Разделители в таблице");
        public JLabel TABLE_TEXT = new JLabel("Файлы таблиц");
        public JLabel DIRECTORY_TEXT = new JLabel("Директория выхода");

        public JButton tableButton = new JButton("...");
        public JButton runButton = new JButton("Поиск по таблицам");
        public JButton directoryButton = new JButton("...");
        public JButton aboutButton = new JButton("О программе");


        public JButton importButton = new JButton("Import all");

        Components() {
            tableButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addTablesFiles();
                }
            });
            runButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startSearch();
                }
            });
            ONLY_SPACE_FIELDS.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    if (!ONLY_SPACE_FIELDS.isSelected()) {
                        regexpField.setEditable(true);
                        regexpField.setEnabled(true);
                    } else {
                        regexpField.setEditable(false);
                        regexpField.setEnabled(false);
                    }
                }
            });
            directoryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addDirectoryFile();
                }
            });
            aboutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    AboutDialog.show(MainFrame.this);
                }
            });

        }
    }


}

