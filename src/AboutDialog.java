import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: 1
 * Date: 02.01.13
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class AboutDialog extends JDialog {
    private static AboutDialog dialog;

    private AboutDialog(JFrame owner) {
        super(owner, "About program", true);

        JLabel label1 = new JLabel("Table explorer v1.0");
        label1.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        label1.setVisible(true);
        Box hBox = Box.createHorizontalBox();
        hBox.add(Box.createRigidArea(new Dimension(30, 50)));
        //hBox.add(Box.createHorizontalGlue());
        hBox.add(label1);
        //hBox.add(Box.createHorizontalGlue());
        add(hBox, BorderLayout.NORTH);



        JLabel[] labels = new JLabel[3];
        labels[0] = new JLabel("<html><h3><b>Автор: Дмитрий Поздин, СПбГУ</b><h3><html>");
        labels[1] = new JLabel("<html><h3><b>e-mail: podmev@gmail.com  </b><h3><html>");
        labels[2] = new JLabel("<html><h4><i>Copyright (c) 2014. All right reserved.</i><h4><html>");
        Box labelBox = Box.createVerticalBox();
        labelBox.add(labels[0]);
        labelBox.add(labels[1]);
        labelBox.add(labels[2]);
        Box mainLabelBox = Box.createHorizontalBox();
        mainLabelBox.add(Box.createHorizontalStrut(30));
        //mainLabelBox.add(Box.createHorizontalGlue());
        mainLabelBox.add(labelBox);
        //mainLabelBox.add(Box.createHorizontalGlue());

        add(mainLabelBox, BorderLayout.CENTER);
/*        JButton ok;
        ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(ok);
        add(buttonPanel, BorderLayout.SOUTH);*/

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(screenSize.width * 3 / 8, screenSize.height * 3 / 8, 400, 250);
        setModal(true);
        setResizable(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                //do nothing
            }
        });

    }

    public static void show(JFrame owner) {
        if (dialog == null) dialog = new AboutDialog(owner);
        dialog.setVisible(true);
    }
}
