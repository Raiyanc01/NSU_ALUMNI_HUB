package javaswing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI {

	private static final String PREF_REMEMBER = "remember_me";
	private static final String PREF_USERNAME = "saved_username";
	private final Preferences prefs = Preferences.userNodeForPackage(GUI.class);

	private static final String[] SECURITY_QUESTIONS = { "What is your mother's maiden name?",
			"What was the name of your first pet?", "What was your primary school name?", "In what city were you born?",
			"What is your favorite book?" };

	public void guiexecute() {
		accountManager.initStorage();

		SwingUtilities.invokeLater(() -> {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(5, 5, 10, 10);
			gbc.weightx = 1.0;

			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setSize(520, 420);
			frame.setLayout(new GridBagLayout());
			frame.setTitle("NSU Alumni Hub 1.0");

			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createTitledBorder(""));
			JLabel label = new JLabel("NSU Alumni Hub", SwingConstants.CENTER);
			label.setFont(new Font("SansSerif", Font.BOLD, 16));
			panel.add(label);

			gbc.gridx = 0;
			gbc.gridy = 0;
			frame.add(panel, gbc);

			JPanel loginpanel = new JPanel(new GridLayout(7, 2, 8, 8));
			loginpanel.setBorder(BorderFactory.createTitledBorder("Login"));

			JLabel label1 = new JLabel("USER ID :");
			JTextField textfield = new JTextField();

			JLabel label2 = new JLabel("Password :");
			JPasswordField password = new JPasswordField(15);

			JCheckBox rememberCheckBox = new JCheckBox("REMEMBER ME");
			JButton forgotPassBtn = new JButton("Forgot Password?");
			forgotPassBtn.setContentAreaFilled(false);
			forgotPassBtn.setBorderPainted(false);
			forgotPassBtn.setForeground(Color.BLUE);

			boolean isRemembered = prefs.getBoolean(PREF_REMEMBER, false);
			if (isRemembered) {
				String savedUser = prefs.get(PREF_USERNAME, "");
				textfield.setText(savedUser);
				rememberCheckBox.setSelected(true);
			}

			JLabel statusLabel = new JLabel("", SwingConstants.LEFT);
			statusLabel.setForeground(Color.RED);

			JButton loginButton = new JButton("Login");
			JButton registerButton = new JButton("Create Account");

			loginButton.addActionListener(e -> {
				String name = textfield.getText().trim();
				String pass = new String(password.getPassword());

				if (name.isEmpty() || pass.isEmpty()) {
					statusLabel.setForeground(Color.RED);
					statusLabel.setText("Please fill all fields!");
					return;
				}

				if (accountManager.isAccountLocked(name)) {
					long remainingMins = accountManager.getRemainingLockoutMinutes(name);
					statusLabel.setForeground(Color.RED);
					statusLabel.setText("Account locked! Try again in " + remainingMins + " mins.");
					return;
				}

				if (accountManager.authenticate(name, pass)) {
					if (rememberCheckBox.isSelected()) {
						prefs.putBoolean(PREF_REMEMBER, true);
						prefs.put(PREF_USERNAME, name);
					} else {
						prefs.putBoolean(PREF_REMEMBER, false);
						prefs.remove(PREF_USERNAME);
					}

					frame.dispose();

					MainDashboard dashboard = new MainDashboard(name);
					dashboard.setVisible(true);
				} else {
					statusLabel.setForeground(Color.RED);
					if (accountManager.isAccountLocked(name)) {
						long remainingMins = accountManager.getRemainingLockoutMinutes(name);
						statusLabel.setText("Account locked for 1 hour! (" + remainingMins + " mins left)");
					} else {
						statusLabel.setText("Invalid credentials or user not found!");
					}
				}
			});

			registerButton.addActionListener(e -> openRegisterDialog(frame));
			forgotPassBtn.addActionListener(e -> openForgotPasswordDialog(frame));

			loginpanel.add(label1);
			loginpanel.add(textfield);
			loginpanel.add(label2);
			loginpanel.add(password);
			loginpanel.add(rememberCheckBox);
			loginpanel.add(forgotPassBtn);
			loginpanel.add(statusLabel);
			loginpanel.add(loginButton);
			loginpanel.add(new JLabel("Don't have an account?"));
			loginpanel.add(registerButton);

			gbc.gridx = 0;
			gbc.gridy = 1;
			frame.add(loginpanel, gbc);

			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	private void openForgotPasswordDialog(JFrame parentFrame) {
		JDialog forgotDialog = new JDialog(parentFrame, "Reset Password", true);
		forgotDialog.setSize(440, 320);
		forgotDialog.setLayout(new BorderLayout(10, 10));
		forgotDialog.setLocationRelativeTo(parentFrame);

		JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
		formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JTextField userF = new JTextField();
		JLabel qLabel = new JLabel("Click 'Fetch Question'");
		qLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
		JTextField answerF = new JTextField();
		JPasswordField newPassF = new JPasswordField();

		JButton fetchQBtn = new JButton("Fetch Question");

		formPanel.add(new JLabel("Username:"));
		formPanel.add(userF);
		formPanel.add(new JLabel(""));
		formPanel.add(fetchQBtn);
		formPanel.add(new JLabel("Security Question:"));
		formPanel.add(qLabel);
		formPanel.add(new JLabel("Your Answer:"));
		formPanel.add(answerF);
		formPanel.add(new JLabel("New Password:"));
		formPanel.add(newPassF);

		JLabel statusMsg = new JLabel("", SwingConstants.CENTER);
		statusMsg.setForeground(Color.RED);

		fetchQBtn.addActionListener(ev -> {
			String u = userF.getText().trim();
			if (u.isEmpty()) {
				statusMsg.setText("Enter username first!");
				return;
			}
			String question = accountManager.getSecurityQuestion(u);
			if (question != null && !question.isEmpty()) {
				qLabel.setText("<html>" + question + "</html>");
				statusMsg.setText("");
			} else {
				statusMsg.setText("User not found or no question configured!");
			}
		});

		JButton resetBtn = new JButton("Reset Password");

		resetBtn.addActionListener(ev -> {
			String user = userF.getText().trim();
			String ans = answerF.getText().trim();
			String newPass = new String(newPassF.getPassword());

			if (user.isEmpty() || ans.isEmpty() || newPass.isEmpty()) {
				statusMsg.setText("Please fill out all fields!");
				return;
			}

			if (accountManager.resetPasswordWithSecurityAnswer(user, ans, newPass)) {
				JOptionPane.showMessageDialog(forgotDialog, "Password reset successfully! You can now log in.");
				forgotDialog.dispose();
			} else {
				statusMsg.setText("Verification failed! Incorrect security answer.");
			}
		});

		JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
		bottomPanel.add(statusMsg, BorderLayout.NORTH);
		bottomPanel.add(resetBtn, BorderLayout.SOUTH);

		forgotDialog.add(formPanel, BorderLayout.CENTER);
		forgotDialog.add(bottomPanel, BorderLayout.SOUTH);

		forgotDialog.setVisible(true);
	}

	private void openRegisterDialog(JFrame parentFrame) {
		JDialog regDialog = new JDialog(parentFrame, "NSU Alumni Registration", true);
		regDialog.setSize(520, 680);
		regDialog.setLayout(new BorderLayout());
		regDialog.setLocationRelativeTo(parentFrame);

		// Updated grid to 17 rows to include Profile Photo selection field
		JPanel formPanel = new JPanel(new GridLayout(17, 2, 6, 6));
		formPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		JTextField usernameF = new JTextField();
		JPasswordField passwordF = new JPasswordField();
		JTextField firstNameF = new JTextField();
		JTextField lastNameF = new JTextField();
		JTextField mobileF = new JTextField();
		JTextField emailF = new JTextField();
		JTextField batchF = new JTextField();
		JTextField nsuIdF = new JTextField();
		JTextField deptF = new JTextField();
		JTextField majorF = new JTextField();
		JTextField profF = new JTextField();
		JTextField desigF = new JTextField();
		JTextField companyF = new JTextField();
		JTextField countryF = new JTextField();

		JComboBox<String> secQuestionCombo = new JComboBox<>(SECURITY_QUESTIONS);
		JTextField secAnswerF = new JTextField();

		// Profile Photo file chooser picker
		JTextField photoPathF = new JTextField();
		photoPathF.setEditable(false);
		JButton browsePhotoBtn = new JButton("Choose Photo...");
		JPanel photoPanel = new JPanel(new BorderLayout(5, 0));
		photoPanel.add(photoPathF, BorderLayout.CENTER);
		photoPanel.add(browsePhotoBtn, BorderLayout.EAST);

		browsePhotoBtn.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (JPG, PNG)", "jpg", "jpeg",
					"png");
			chooser.setFileFilter(filter);
			if (chooser.showOpenDialog(regDialog) == JFileChooser.APPROVE_OPTION) {
				photoPathF.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});

		formPanel.add(new JLabel("Username:"));
		formPanel.add(usernameF);
		formPanel.add(new JLabel("Password:"));
		formPanel.add(passwordF);
		formPanel.add(new JLabel("First Name:"));
		formPanel.add(firstNameF);
		formPanel.add(new JLabel("Last Name:"));
		formPanel.add(lastNameF);
		formPanel.add(new JLabel("Mobile Number:"));
		formPanel.add(mobileF);
		formPanel.add(new JLabel("Email:"));
		formPanel.add(emailF);
		formPanel.add(new JLabel("Semester Batch:"));
		formPanel.add(batchF);
		formPanel.add(new JLabel("NSU ID:"));
		formPanel.add(nsuIdF);
		formPanel.add(new JLabel("Department:"));
		formPanel.add(deptF);
		formPanel.add(new JLabel("Major:"));
		formPanel.add(majorF);
		formPanel.add(new JLabel("Profession:"));
		formPanel.add(profF);
		formPanel.add(new JLabel("Designation:"));
		formPanel.add(desigF);
		formPanel.add(new JLabel("Company Name:"));
		formPanel.add(companyF);
		formPanel.add(new JLabel("Country Live In:"));
		formPanel.add(countryF);
		formPanel.add(new JLabel("Security Question:"));
		formPanel.add(secQuestionCombo);
		formPanel.add(new JLabel("Security Answer:"));
		formPanel.add(secAnswerF);
		formPanel.add(new JLabel("Profile Photo:"));
		formPanel.add(photoPanel);

		JButton submitBtn = new JButton("Register");
		JLabel status = new JLabel("", SwingConstants.CENTER);
		status.setForeground(Color.RED);

		submitBtn.addActionListener(ev -> {
			String user = usernameF.getText().trim();
			String pass = new String(passwordF.getPassword());
			String mobile = mobileF.getText().trim();
			String email = emailF.getText().trim();
			String nsuId = nsuIdF.getText().trim();
			String question = (String) secQuestionCombo.getSelectedItem();
			String answer = secAnswerF.getText().trim();
			String photoPath = photoPathF.getText().trim();

			if (user.isEmpty() || pass.isEmpty() || firstNameF.getText().trim().isEmpty()) {
				status.setText("Username, Password, and First Name are required!");
				return;
			}

			if (answer.isEmpty()) {
				status.setText("Security Answer is required!");
				return;
			}

			String duplicateError = accountManager.checkDuplicates(user, nsuId, mobile, email);
			if (duplicateError != null) {
				status.setText(duplicateError);
				return;
			}

			if (accountManager.registerUser(user, pass, firstNameF.getText(), lastNameF.getText(), mobile, email,
					batchF.getText(), nsuId, deptF.getText(), majorF.getText(), profF.getText(), desigF.getText(),
					companyF.getText(), countryF.getText(), question, answer, photoPath)) {
				JOptionPane.showMessageDialog(regDialog, "Registration Successful! You can now log in.");
				regDialog.dispose();
			} else {
				status.setText("Failed to save registration data.");
			}
		});

		JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
		bottomPanel.add(status, BorderLayout.NORTH);
		bottomPanel.add(submitBtn, BorderLayout.SOUTH);

		regDialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
		regDialog.add(bottomPanel, BorderLayout.SOUTH);

		regDialog.setVisible(true);
	}
}