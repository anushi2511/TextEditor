import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class NewTextEditor extends JFrame {
    private JTextPane textPane;
    private JFileChooser fileChooser;
    private JPanel shapePanel;
    private DrawingCanvas canvas;
    private String clipboard;

    public NewTextEditor() {
        setTitle("Text Editor with Shapes");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Setup text area
        textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        // Setup shapes panel
        shapePanel = new JPanel();
        shapePanel.setLayout(new GridLayout(6, 1, 3, 3)); // 6 rows, 1 column
        String[] shapes = {"Rectangle", "Oval", "Line", "Triangle", "Pentagon", "CLEAR"};
        for (String shape : shapes) {
            createStyledButton(shape);
        }

        // Drawing canvas
        canvas = new DrawingCanvas();
        canvas.setPreferredSize(new Dimension(400, 600));

        // Combine canvas and shape panel into one panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(canvas, BorderLayout.CENTER);
        rightPanel.add(shapePanel, BorderLayout.EAST);

        // Use JSplitPane to separate the text area and the right panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, rightPanel);
        splitPane.setDividerLocation(550); // Adjust initial divider position
        add(splitPane);

        // File chooser
        fileChooser = new JFileChooser();

        // Setup menu
        setupMenu();

        setVisible(true);
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("    File    ");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);

        newItem.addActionListener(e -> openNewFile());
        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());

        // Edit Menu
        JMenu editMenu = new JMenu("    Edit    ");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem pasteItem = new JMenuItem("Paste");
        JMenuItem findReplaceFirstItem = new JMenuItem("Find & Replace First");
        JMenuItem findReplaceItem = new JMenuItem("Find & Replace All");
        JMenuItem wordCountItem = new JMenuItem("Word Count");
        JMenuItem charCountItem = new JMenuItem("Character Count");

        editMenu.add(cutItem);
        editMenu.addSeparator();
        editMenu.add(copyItem);
        editMenu.addSeparator();
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(findReplaceFirstItem);
        editMenu.addSeparator();
        editMenu.add(findReplaceItem);
        editMenu.addSeparator();
        editMenu.add(wordCountItem);
        editMenu.addSeparator();
        editMenu.add(charCountItem);
        menuBar.add(editMenu);

        cutItem.addActionListener(e -> cutText());
        copyItem.addActionListener(e -> copyText());
        pasteItem.addActionListener(e -> pasteText());
        findReplaceFirstItem.addActionListener(e -> findAndReplaceFirst());
        findReplaceItem.addActionListener(e -> findAndReplace());
        wordCountItem.addActionListener(e -> wordCount());
        charCountItem.addActionListener(e -> charCount());

        // Format Menu
        JMenu formatMenu = new JMenu("    Format    ");
        JMenuItem fontItem = new JMenuItem("Font");
        JMenuItem fontSize = new JMenuItem("Font Size");
        JMenuItem fontStyle = new JMenuItem("Font Style");
        JMenuItem fontColour = new JMenuItem("Font Colour");
        JMenuItem changeCaseItem = new JMenuItem("Text Case");

        formatMenu.add(fontItem);
        formatMenu.addSeparator();
        formatMenu.add(fontSize);
        formatMenu.addSeparator();
        formatMenu.add(fontStyle);
        formatMenu.addSeparator();
        formatMenu.add(fontColour);
        formatMenu.addSeparator();
        formatMenu.add(changeCaseItem);
        menuBar.add(formatMenu);

        fontItem.addActionListener(e -> changeFontName());
        fontStyle.addActionListener(e -> changeFontStyle());
        fontSize.addActionListener(e -> changeFontSize());
        fontColour.addActionListener(e -> changeFontColour());
        changeCaseItem.addActionListener(e -> changeTextCase());

        JButton toggleDarkModeButton = new JButton("Toggle Dark Mode");
        toggleDarkModeButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        toggleDarkModeButton.addActionListener(e -> toggleDarkMode());
        toggleDarkModeButton.setFocusPainted(false);
        toggleDarkModeButton.setBorderPainted(false); // Remove button border
        toggleDarkModeButton.setBackground(Color.LIGHT_GRAY);
        toggleDarkModeButton.setForeground(Color.BLACK);
        menuBar.add(Box.createHorizontalGlue()); // Align button to the right
        menuBar.add(toggleDarkModeButton);

        setJMenuBar(menuBar);

        Font menuFont = new Font("Segoe UI", Font.BOLD, 16);
        fileMenu.setFont(menuFont);
        editMenu.setFont(menuFont);
        formatMenu.setFont(menuFont);
        Font itemFont = new Font("Segoe UI", Font.PLAIN, 14);
        newItem.setFont(itemFont);
        saveItem.setFont(itemFont);
        openItem.setFont(itemFont);
        cutItem.setFont(itemFont);
        copyItem.setFont(itemFont);
        pasteItem.setFont(itemFont);
        findReplaceFirstItem.setFont(itemFont);
        findReplaceItem.setFont(itemFont);
        wordCountItem.setFont(itemFont);
        charCountItem.setFont(itemFont);
        fontItem.setFont(itemFont);
        fontSize.setFont(itemFont);
        fontStyle.setFont(itemFont);
        fontColour.setFont(itemFont);
        changeCaseItem.setFont(itemFont);
    }

    private void openNewFile() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save the current file before opening a new one?",
                "Save File",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            saveFile();
        } else if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return; // Exit the function if user cancels
        }

        // Clear the text component
        textPane.setText("");
        setTitle("Text Editor with Shapes");
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                textPane.setText(content.toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error reading file: " + ex.getMessage(),
                        "File Open Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();

                // Ensure the file has a .txt extension if no extension is specified
                if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));
                writer.write(textPane.getText());
                writer.close();

                JOptionPane.showMessageDialog(this,
                        "File saved successfully!",
                        "Save Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving file: " + ex.getMessage(),
                        "File Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cutText() {
        clipboard = textPane.getSelectedText();
        if (clipboard != null) {
            textPane.replaceSelection(""); // Removes the selected text
        } else {
            JOptionPane.showMessageDialog(this, "No text selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyText() {
        clipboard = textPane.getSelectedText();
        if (clipboard == null) {
            JOptionPane.showMessageDialog(this, "No text selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pasteText() {
        if (clipboard != null && !clipboard.isEmpty()) {
            textPane.replaceSelection(clipboard); // Inserts clipboard content at the caret position
        } else {
            JOptionPane.showMessageDialog(this, "Clipboard is empty.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void findAndReplaceFirst() {
        // Prompt user for the text to find and replace
        String find = JOptionPane.showInputDialog(this, "Enter text to find:");
        if (find == null || find.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No text entered.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String replace = JOptionPane.showInputDialog(this, "Enter text to replace with:");
        if (replace == null) {
            JOptionPane.showMessageDialog(this, "No replacement text entered.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            // Get the document text
            String documentText = textPane.getText();

            // Find the first occurrence of the text
            int matchIndex = documentText.indexOf(find);
            if (matchIndex != -1) {
                // Replace the first occurrence
                String updatedText = documentText.substring(0, matchIndex) +
                        replace +
                        documentText.substring(matchIndex + find.length());

                // Update the document
                textPane.setText(updatedText);

                // Highlight the replaced text
                textPane.requestFocus();
                textPane.select(matchIndex, matchIndex + replace.length());
            } else {
                // Text not found
                JOptionPane.showMessageDialog(this, "\"" + find + "\" not found.", "Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An error occurred during the find and replace.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void findAndReplace() {
        String find = JOptionPane.showInputDialog(this, "Find:");
        String replace = JOptionPane.showInputDialog(this, "Replace with:");

        if (find != null && replace != null) {
            String content = textPane.getText();
            if (content.contains(find)) {
                textPane.setText(content.replace(find, replace)); // Replaces all occurrences
            } else {
                JOptionPane.showMessageDialog(this, "Text not found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void wordCount() {
        String selectedText = textPane.getSelectedText();
        if (selectedText != null) {
            int wordCount = selectedText.trim().isEmpty() ? 0 : selectedText.split("\\s+").length; // Counts words
            JOptionPane.showMessageDialog(this, "No. of Words: " + wordCount);
        } else {
            JOptionPane.showMessageDialog(this, "No text selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void charCount() {
        String selectedText = textPane.getSelectedText();
        if (selectedText != null) {
            int charCount = selectedText.length(); // Counts characters
            JOptionPane.showMessageDialog(this, "No. of Characters: " + charCount);
        } else {
            JOptionPane.showMessageDialog(this, "No text selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void changeFontName() {
        // Get all available fonts
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();

        // Show font selection dialog
        String selectedFont = (String) JOptionPane.showInputDialog(
                this,
                "Choose Font:",
                "Font Name",
                JOptionPane.QUESTION_MESSAGE,
                null,
                fontNames,
                textPane.getFont().getName() // Default selection
        );

        if (selectedFont != null) {
            // Apply font change to selected text
            StyledDocument doc = textPane.getStyledDocument();
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();

            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attributes, selectedFont);
            doc.setCharacterAttributes(start, end - start, attributes, false);
        }
    }

    private void changeFontSize() {
        String input = JOptionPane.showInputDialog(this, "Enter Font Size:");
        if (input != null) {
            try {
                int newSize = Integer.parseInt(input);
                if (newSize > 0) {
                    StyledDocument doc = textPane.getStyledDocument();
                    int start = textPane.getSelectionStart();
                    int end = textPane.getSelectionEnd();

                    SimpleAttributeSet attributes = new SimpleAttributeSet();
                    StyleConstants.setFontSize(attributes, newSize);
                    doc.setCharacterAttributes(start, end - start, attributes, false);
                } else {
                    JOptionPane.showMessageDialog(this, "Font size must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid font size.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeFontStyle() {
        String[] styles = {"Plain", "Bold", "Italic"};
        String selectedStyle = (String) JOptionPane.showInputDialog(
                this,
                "Choose Font Style:",
                "Font Style",
                JOptionPane.QUESTION_MESSAGE,
                null,
                styles,
                styles[0]
        );

        if (selectedStyle != null) {
            int fontStyle = switch (selectedStyle) {
                case "Bold" -> Font.BOLD;
                case "Italic" -> Font.ITALIC;
                default -> Font.PLAIN;
            };

            StyledDocument doc = textPane.getStyledDocument();
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();

            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setBold(attributes, fontStyle == Font.BOLD);
            StyleConstants.setItalic(attributes, fontStyle == Font.ITALIC);
            doc.setCharacterAttributes(start, end - start, attributes, false);
        }
    }

    private void changeFontColour() {
        Color selectedColor = JColorChooser.showDialog(this, "Choose Font Colour", textPane.getForeground());
        if (selectedColor != null) {
            StyledDocument doc = textPane.getStyledDocument();
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();

            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setForeground(attributes, selectedColor);
            doc.setCharacterAttributes(start, end - start, attributes, false);
        }
    }

    private void changeTextCase() {
        String selectedText = textPane.getSelectedText();
        if (selectedText != null) {
            // Offer user the choice to convert to Uppercase or Lowercase
            String[] options = {"Uppercase", "Lowercase"};
            String choice = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose case transformation:",
                    "Change Text Case",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0] // Default selection
            );

            if (choice != null) {
                String transformedText = switch (choice) {
                    case "Uppercase" -> selectedText.toUpperCase();
                    case "Lowercase" -> selectedText.toLowerCase();
                    default -> selectedText;
                };

                // Replace the selected text with the transformed text
                try {
                    int start = textPane.getSelectionStart();
                    StyledDocument doc = textPane.getStyledDocument();
                    doc.remove(start, selectedText.length()); // Remove original text
                    doc.insertString(start, transformedText, textPane.getCharacterAttributes()); // Insert transformed text
                } catch (BadLocationException e) {
                    JOptionPane.showMessageDialog(this, "Error changing text case.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No text selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    class DrawingCanvas extends JPanel {
        private String shapeToDraw = "CLEAR";
        private Point startPoint, endPoint;
        private ArrayList<Shape> shapes = new ArrayList<>();

        public DrawingCanvas() {
            setPreferredSize(new Dimension(400, 600));
            setBackground(Color.WHITE);

            // Mouse listeners for drawing
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    endPoint = e.getPoint();
                    if (!"CLEAR".equals(shapeToDraw)) {
                        shapes.add(createShape());
                    }
                    repaint();
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    endPoint = e.getPoint();
                    repaint();
                }
            });
        }

        public void setShapeToDraw(String shape) {
            this.shapeToDraw = shape;
            if ("CLEAR".equals(shape)) {
                shapes.clear();
                repaint();
            }
        }

        private Shape createShape() {
            if (startPoint == null || endPoint == null) return null;

            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            int width = Math.abs(startPoint.x - endPoint.x);
            int height = Math.abs(startPoint.y - endPoint.y);

            switch (shapeToDraw) {
                case "Rectangle":
                    return new Rectangle(x, y, width, height);
                case "Oval":
                    return new Ellipse2D.Double(x, y, width, height);
                case "Line":
                    return new java.awt.geom.Line2D.Double(startPoint, endPoint);
                case "Triangle":
                    return createTriangle(startPoint, endPoint);
                case "Pentagon":
                    return createPentagon(startPoint, endPoint);
                default:
                    return null;
            }
        }

        private Shape createTriangle(Point start, Point end) {
            int x1 = start.x, y1 = start.y;
            int x2 = end.x, y2 = end.y;
            int midX = (x1 + x2) / 2;
            Path2D path = new Path2D.Double();
            path.moveTo(midX, y1); // Top
            path.lineTo(x1, y2);   // Bottom left
            path.lineTo(x2, y2);   // Bottom right
            path.closePath();
            return path;
        }

        private Shape createPentagon(Point start, Point end) {
            int x1 = start.x, y1 = start.y;
            int x2 = end.x, y2 = end.y;
            int width = Math.abs(x2 - x1);
            int height = Math.abs(y2 - y1);

            int midX = (x1 + x2) / 2;
            int[] xPoints = {midX, x1, x1 + width / 4, x2 - width / 4, x2};
            int[] yPoints = {y1, y1 + height / 3, y2, y2, y1 + height / 3};
            return new Polygon(xPoints, yPoints, 5);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // Existing shape drawing code
            g2.setColor(Color.BLACK);

            // Draw all shapes
            for (Shape shape : shapes) {
                g2.draw(shape);
            }

            // Draw the current shape being dragged
            if (startPoint != null && endPoint != null && !"CLEAR".equals(shapeToDraw)) {
                g2.setColor(Color.GRAY);
                g2.draw(createShape());
            }
        }
    }

    private boolean isDarkMode = false; // Flag to track the current theme

    private void toggleDarkMode() {
        if (isDarkMode) {
            // Switch to light mode
            textPane.setBackground(Color.WHITE);
            textPane.setForeground(Color.BLACK);
            textPane.setCaretColor(Color.BLACK);

            if (canvas != null) {
                canvas.setBackground(Color.WHITE);
            }

            JMenuBar menuBar = getJMenuBar();
            menuBar.setBackground(Color.WHITE);
            menuBar.setForeground(Color.BLACK);

            for (Component menu : menuBar.getComponents()) {
                if (menu instanceof JMenu) {
                    menu.setBackground(Color.WHITE);
                    menu.setForeground(Color.BLACK);
                    for (Component menuItem : ((JMenu) menu).getMenuComponents()) {
                        menuItem.setBackground(Color.WHITE);
                        menuItem.setForeground(Color.BLACK);
                    }
                }
            }
            UIManager.put("Menu.background", Color.WHITE);
            UIManager.put("Menu.foreground", Color.BLACK);
            UIManager.put("MenuItem.background", Color.WHITE);
            UIManager.put("MenuItem.foreground", Color.BLACK);
            UIManager.put("Panel.background", Color.LIGHT_GRAY);
            UIManager.put("Panel.foreground", Color.BLACK);
            UIManager.put("ScrollPane.background", Color.LIGHT_GRAY);
            UIManager.put("ScrollPane.foreground", Color.BLACK);
        } else {
            // Switch to dark mode
            textPane.setBackground(Color.DARK_GRAY);
            textPane.setForeground(Color.WHITE);
            textPane.setCaretColor(Color.WHITE);

            if (canvas != null) {
                canvas.setBackground(Color.DARK_GRAY); // Apply dark mode to canvas
            }

            JMenuBar menuBar = getJMenuBar();
            menuBar.setBackground(Color.BLACK);
            menuBar.setForeground(Color.WHITE);

            for (Component menu : menuBar.getComponents()) {
                if (menu instanceof JMenu) {
                    menu.setBackground(Color.DARK_GRAY);
                    menu.setForeground(Color.WHITE);
                    for (Component menuItem : ((JMenu) menu).getMenuComponents()) {
                        menuItem.setBackground(Color.DARK_GRAY);
                        menuItem.setForeground(Color.WHITE);
                    }
                }
            }

            UIManager.put("Menu.background", Color.DARK_GRAY);
            UIManager.put("Menu.foreground", Color.WHITE);
            UIManager.put("MenuItem.background", Color.DARK_GRAY);
            UIManager.put("MenuItem.foreground", Color.WHITE);
            UIManager.put("Panel.background", Color.BLACK);
            UIManager.put("Panel.foreground", Color.WHITE);
            UIManager.put("ScrollPane.background", Color.BLACK);
            UIManager.put("ScrollPane.foreground", Color.WHITE);
        }

        // Refresh the UI to apply changes
        SwingUtilities.updateComponentTreeUI(this);

        isDarkMode = !isDarkMode; // Toggle the flag
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(83, 90, 218));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(115, 118, 200, 255));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(83, 90, 218));
            }
        });

        shapePanel.add(button);
        button.addActionListener(e -> canvas.setShapeToDraw(text));

        return button;
    }
}