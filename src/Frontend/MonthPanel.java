package Frontend;

import Backend.Match;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MonthPanel extends JPanel {

    private java.time.LocalDate monthStart;

    private List<DayPanel> dayPanels; //TODO: can probably use JPanel::getComponent(int)

    /**
     * Default Constructor
     */
    public MonthPanel() {
        this(2018, 1, Collections.emptyList());
    }

    /**
     * @param year     to initialize the MonthPanel to
     * @param monthNum to initialize the MonthPanel to
     * @param matches  to add to the DayPanels
     */
    public MonthPanel(int year, int monthNum, List<Match> matches) {
        this.setPreferredSize(new Dimension(1000, 800));
        this.setLayout(new GridLayout(5, 7, 10, 10));
        this.dayPanels = new ArrayList<>();

        setToMonth(year, monthNum);
        setMatchesOnDayPanels(
                matches.stream().filter(
                        match -> (match.getMatchDate().getYear() == monthStart.getYear() && match.getMatchDate().getMonth().equals(monthStart.getMonth()))
                ).collect(Collectors.toList()));
    }

    /**
     * This method clears and resets the DayPanels to the month selected
     *
     * @param year
     * @param monthNum
     */
    private void setToMonth(int year, int monthNum) {
        this.monthStart = java.time.LocalDate.of(year, monthNum, 1);

        //if the first day of the month is not a sunday, we need empty space to pad out the grid cells
        //getDayOfWeek returns a java.time.DayOfWeek enum where 1 = Monday and 7 = Sunday
        //We want sunday to be the leftmost column, so do getDayOfWeek % 7 to find how many empty cells we need to pad
        int dayOffset = monthStart.getDayOfWeek().getValue() % 7;
        int daysInMonth = monthStart.lengthOfMonth();

        this.removeAll(); // make sure MonthPanel has no child components
        for (int i = 0; i < daysInMonth + dayOffset; i++) {
            //when the first day of the month is not a sunday, there will be empty space in the calendar
            if (i < dayOffset) {
                //use a label for empty space
                this.add(new JLabel());
            } else {
                //Grid cells are 0 indexed, but month days aren't. Subtract the offset to get the actual date.
                LocalDate date = monthStart.withDayOfMonth(i + 1 - dayOffset);
                DayPanel dayPanel = new DayPanel(date);
                dayPanels.add(dayPanel);
                this.add(dayPanel, i);
            }
        }
        for (int i = daysInMonth + dayOffset; i < 35; i++) {
            this.add(new JLabel());
        }
    }

    /**
     * This method adds matches to DayPanels
     *
     * @param matches to add
     */
    private void setMatchesOnDayPanels(List<Match> matches) {
        for (Match match : matches) {
            if (match.getMatchDate().getYear() != monthStart.getYear() || match.getMatchDate().getMonth().equals(monthStart.getMonth()))
                return; //fail gracefully if match being added is not on the right
            DayPanel dayPanel = dayPanels.get(match.getMatchDate().getDayOfMonth() - 1);
            if (dayPanel != null) {
                System.out.printf("adding match to day %d\n", match.getMatchDate().getDayOfMonth());
                dayPanel.addMatch(match);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    /**
     * Inner class representing one day in a MonthPanel
     */
    private class DayPanel extends JPanel {
        private final java.time.LocalDate date;
        private List<Match> matches;
        private GridBagConstraints labelConstraints;

        /**
         * Creates a new DayPanel, and adds a date JLabel to it
         *
         * @param date to initialize the DayPanel to
         */
        public DayPanel(java.time.LocalDate date) {
            //MAX OF 7 MATCHES PER DAY
            this.setLayout(new GridBagLayout()); //TODO: use gridbaglayout
            this.setSize(new Dimension(100, 100));
            matches = new ArrayList<>();
            this.date = LocalDate.from(date);

            labelConstraints = new GridBagConstraints();
            labelConstraints.anchor = GridBagConstraints.NORTHWEST;
            labelConstraints.fill = GridBagConstraints.HORIZONTAL;
            labelConstraints.gridx = 0; //position
            labelConstraints.gridy = 0; //position
            labelConstraints.insets = new Insets(4, 4, 4, 4);
            labelConstraints.gridwidth = 1;
            labelConstraints.gridheight = 1;
            labelConstraints.weightx = 0.8;
            labelConstraints.weighty = 1.0;

            addLabel(new JLabel(String.valueOf(date.getDayOfMonth())));

            labelConstraints.weighty = 0; //make match labels go to bottom of screen
            labelConstraints.anchor = GridBagConstraints.SOUTH;
        }

        private void addLabel(JLabel label) {
            this.add(label, labelConstraints);
            labelConstraints.gridy = labelConstraints.gridy + 1;
        }

        private void addLabel(JLabel leftImage, JLabel matchLabel, JLabel rightImage) {
            labelConstraints.anchor = GridBagConstraints.SOUTHWEST;
            labelConstraints.gridx = 0;
            this.add(leftImage, labelConstraints);
            labelConstraints.anchor = GridBagConstraints.SOUTH;
            labelConstraints.gridx = 1;
            this.add(matchLabel, labelConstraints);
            labelConstraints.anchor = GridBagConstraints.SOUTHEAST;
            labelConstraints.gridx = 2;
            this.add(rightImage, labelConstraints);
            labelConstraints.gridy = labelConstraints.gridy + 1;
        }

        /**
         * assert that all matches in the DayPanel are actually on thate date
         *
         * @return true if the class invariant is not violated
         */
        private boolean classInv() {
            boolean flag = true;
            for (Match match : matches) {
                flag &= date.isEqual(match.getMatchDate());
            }
            return flag;
        }

        private JLabel loadFlagLabel(String teamAbbv) {
            JLabel flagIcon = new JLabel();
            File imgFile = new File("Assets" + File.separator + "Images" + File.separator + "smallFlags" + File.separator + teamAbbv + ".png");
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(imgFile);
                flagIcon.setIcon(getScaledIconFromImage(bufferedImage));
            } catch (IOException e) {
                System.err.printf("can't find flag for %s\n", teamAbbv);
            }

            return flagIcon;
        }

        private ImageIcon getScaledIconFromImage(Image img) {
            double scaleFactor = 0.25;
            int iconX = (int) (img.getWidth(this) * scaleFactor);
            int iconY = (int) (img.getHeight(this) * scaleFactor);

            Image scaledPreviewImage = img.getScaledInstance(iconX, iconY, Image.SCALE_SMOOTH);
            BufferedImage image = new BufferedImage(iconX, iconY, BufferedImage.TYPE_INT_ARGB);

            image.getGraphics().drawImage(scaledPreviewImage, 0, 0, null);

            return new ImageIcon(image);
        }

        /**
         * Add a match to the DayPanel.
         * This method adds mouselisteners to each Match label to handle the tooltips
         *
         * @param match to add
         */
        public void addMatch(Match match) {
            System.out.printf("adding match %s\n", match);
            this.matches.add(match);

            JLabel leftFlag = loadFlagLabel(match.getTeamOne().getAbbv());
            leftFlag.setToolTipText(match.getTeamOne().toString());
            JLabel rightFlag = loadFlagLabel(match.getTeamTwo().getAbbv());
            rightFlag.setToolTipText(match.getTeamTwo().toString());


            //add a mouseover to display JPopupMenu
            JLabel matchLabel = new JLabel();
            matchLabel.setText(String.format("%s v. %s", match.getTeamOne().getAbbv(), match.getTeamTwo().getAbbv()));
            matchLabel.setToolTipText(match.toString());
            matchLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    System.out.printf("entered %s\n", matchLabel.getText());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    System.out.printf("exited %s\n", matchLabel.getText());
                }
            });


            addLabel(leftFlag, matchLabel, rightFlag);
            assert classInv();

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.lightGray);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

    }
}
