package javaswing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
	private Preferences prefs;

	private static final String[] SECURITY_QUESTIONS = { "Who is your first crush?",
			"What was the name of your first pet?", "What was your primary school name?", "In what city were you born?",
			"What is your favorite book?" };

	public GUI() {
		try {
			this.prefs = Preferences.userNodeForPackage(GUI.class);
		} catch (Exception e) {
			System.err.println("Failed to load user preferences: " + e.getMessage());
			this.prefs = null;
		}
	}

	public void guiexecute() {
		try {
			accountManager.initStorage();
		} catch (Exception e) {
			System.err.println("Failed to initialize account storage: " + e.getMessage());
			JOptionPane.showMessageDialog(null, 
					"Error initializing application database: " + e.getMessage(), 
					"Database Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		SwingUtilities.invokeLater(() -> {
			try {
				JFrame frame = new JFrame("NSU Alumni Hub 1.0");
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setSize(480, 560);
				frame.setLocationRelativeTo(null);
				frame.setLayout(new BorderLayout());

				JPanel headerPanel = new JPanel(new GridBagLayout());
				headerPanel.setBackground(new Color(24, 119, 242));
				headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 110));

				GridBagConstraints hgbc = new GridBagConstraints();
				hgbc.gridx = 0;
				hgbc.gridy = 0;
				hgbc.insets = new Insets(5, 5, 5, 5);

				JLabel logoLabel = createLogoLabel("logo.png", 50, 50);
				headerPanel.add(logoLabel, hgbc);

				hgbc.gridy = 1;
				JLabel titleLabel = new JLabel("NSU Alumni Hub");
				titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
				titleLabel.setForeground(Color.WHITE);
				headerPanel.add(titleLabel, hgbc);

				frame.add(headerPanel, BorderLayout.NORTH);

				JPanel formCard = new JPanel(new GridBagLayout());
				formCard.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.insets = new Insets(6, 6, 6, 6);
				gbc.weightx = 1.0;

				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.gridwidth = 2;
				JLabel userLbl = new JLabel("USER ID / Username");
				userLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
				formCard.add(userLbl, gbc);

				gbc.gridy = 1;
				JTextField textfield = new JTextField(20);
				textfield.setPreferredSize(new Dimension(0, 32));
				formCard.add(textfield, gbc);

				gbc.gridy = 2;
				JLabel passLbl = new JLabel("Password");
				passLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
				formCard.add(passLbl, gbc);

				gbc.gridy = 3;
				JPasswordField password = new JPasswordField(20);
				password.setPreferredSize(new Dimension(0, 32));
				formCard.add(password, gbc);

				gbc.gridy = 4;
				gbc.gridwidth = 1;
				JCheckBox rememberCheckBox = new JCheckBox("Remember Me");
				rememberCheckBox.setFocusPainted(false);
				formCard.add(rememberCheckBox, gbc);

				gbc.gridx = 1;
				JButton forgotPassBtn = new JButton("Forgot Password?");
				forgotPassBtn.setContentAreaFilled(false);
				forgotPassBtn.setBorderPainted(false);
				forgotPassBtn.setFocusPainted(false);
				forgotPassBtn.setForeground(new Color(24, 119, 242));
				forgotPassBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
				forgotPassBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				formCard.add(forgotPassBtn, gbc);

				gbc.gridx = 0;
				gbc.gridy = 5;
				gbc.gridwidth = 2;
				JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
				statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
				statusLabel.setForeground(Color.RED);
				formCard.add(statusLabel, gbc);

				gbc.gridy = 6;
				JButton loginButton = new JButton("Login");
				loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
				loginButton.setBackground(new Color(24, 119, 242));
				loginButton.setForeground(Color.WHITE);
				loginButton.setFocusPainted(false);
				loginButton.setPreferredSize(new Dimension(0, 38));
				styleInteractiveBtn(loginButton, new Color(24, 119, 242), new Color(15, 90, 200));
				formCard.add(loginButton, gbc);

				gbc.gridy = 7;
				JButton registerButton = new JButton("Create Account");
				registerButton.setFont(new Font("SansSerif", Font.BOLD, 13));
				registerButton.setBackground(new Color(40, 167, 69));
				registerButton.setForeground(Color.WHITE);
				registerButton.setFocusPainted(false);
				registerButton.setPreferredSize(new Dimension(0, 36));
				styleInteractiveBtn(registerButton, new Color(40, 167, 69), new Color(30, 130, 50));
				formCard.add(registerButton, gbc);

				if (prefs != null) {
					try {
						boolean isRemembered = prefs.getBoolean(PREF_REMEMBER, false);
						if (isRemembered) {
							String savedUser = prefs.get(PREF_USERNAME, "");
							textfield.setText(savedUser);
							rememberCheckBox.setSelected(true);
						}
					} catch (Exception ex) {
						System.err.println("Error reading preferences: " + ex.getMessage());
					}
				}

				loginButton.addActionListener(e -> {
					try {
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
							if (prefs != null) {
								try {
									if (rememberCheckBox.isSelected()) {
										prefs.putBoolean(PREF_REMEMBER, true);
										prefs.put(PREF_USERNAME, name);
									} else {
										prefs.putBoolean(PREF_REMEMBER, false);
										prefs.remove(PREF_USERNAME);
									}
								} catch (Exception prefEx) {
									System.err.println("Failed to update preferences: " + prefEx.getMessage());
								}
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
					} catch (Exception ex) {
						statusLabel.setText("An unexpected error occurred during login.");
						System.err.println("Authentication execution error: " + ex.getMessage());
					}
				});

				registerButton.addActionListener(e -> openRegisterDialog(frame));
				forgotPassBtn.addActionListener(e -> openForgotPasswordDialog(frame));

				frame.add(formCard, BorderLayout.CENTER);
				frame.setVisible(true);

			} catch (Exception ex) {
				System.err.println("Fatal GUI construction error: " + ex.getMessage());
				JOptionPane.showMessageDialog(null, 
						"Critical error starting application UI: " + ex.getMessage(), 
						"Startup Error", JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	private JLabel createLogoLabel(String path, int width, int height) {
		JLabel label = new JLabel();
		try {
			File logoFile = new File(path);
			if (logoFile.exists()) {
				ImageIcon icon = new ImageIcon(path);
				Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
				label.setIcon(new ImageIcon(img));
				return label;
			}
		} catch (Exception e) {
			System.err.println("Warning: Unable to render logo image (" + e.getMessage() + ")");
		}

		label.setText("🎓");
		label.setFont(new Font("SansSerif", Font.PLAIN, 40));
		label.setForeground(Color.WHITE);
		return label;
	}

	private void styleInteractiveBtn(JButton button, Color baseColor, Color hoverColor) {
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setBackground(hoverColor);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(baseColor);
			}
		});
	}

	private void openForgotPasswordDialog(JFrame parentFrame) {
		try {
			JDialog forgotDialog = new JDialog(parentFrame, "Reset Password", true);
			forgotDialog.setSize(440, 320);
			forgotDialog.setLayout(new BorderLayout(10, 10));
			forgotDialog.setLocationRelativeTo(parentFrame);

			JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
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
			formPanel.add(new JLabel(""));
			JLabel newPassHintLbl = new JLabel(
					"<html><font size='2' color='gray'>Min 8 chars, upper/lowercase, number &amp; special char</font></html>");
			formPanel.add(newPassHintLbl);

			JLabel statusMsg = new JLabel("", SwingConstants.CENTER);
			statusMsg.setForeground(Color.RED);

			fetchQBtn.addActionListener(ev -> {
				try {
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
				} catch (Exception ex) {
					statusMsg.setText("Error retrieving security question.");
					System.err.println("Fetch Question Error: " + ex.getMessage());
				}
			});

			JButton resetBtn = new JButton("Reset Password");

			resetBtn.addActionListener(ev -> {
				try {
					String user = userF.getText().trim();
					String ans = answerF.getText().trim();
					String newPass = new String(newPassF.getPassword());

					if (user.isEmpty() || ans.isEmpty() || newPass.isEmpty()) {
						statusMsg.setText("Please fill out all fields!");
						return;
					}

					String passwordError = accountManager.validatePasswordStrength(newPass);
					if (passwordError != null) {
						statusMsg.setText(passwordError);
						return;
					}

					if (accountManager.resetPasswordWithSecurityAnswer(user, ans, newPass)) {
						JOptionPane.showMessageDialog(forgotDialog, "Password reset successfully! You can now log in.");
						forgotDialog.dispose();
					} else {
						statusMsg.setText("Verification failed! Incorrect security answer.");
					}
				} catch (Exception ex) {
					statusMsg.setText("An error occurred resetting password.");
					System.err.println("Password Reset Error: " + ex.getMessage());
				}
			});

			JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
			bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
			bottomPanel.add(statusMsg, BorderLayout.NORTH);
			bottomPanel.add(resetBtn, BorderLayout.SOUTH);

			forgotDialog.add(formPanel, BorderLayout.CENTER);
			forgotDialog.add(bottomPanel, BorderLayout.SOUTH);

			forgotDialog.setVisible(true);
		} catch (Exception ex) {
			System.err.println("Error displaying password reset dialog: " + ex.getMessage());
		}
	}

	private void openRegisterDialog(JFrame parentFrame) {
		try {
			JDialog regDialog = new JDialog(parentFrame, "NSU Alumni Registration", true);
			regDialog.setSize(520, 680);
			regDialog.setLayout(new BorderLayout());
			regDialog.setLocationRelativeTo(parentFrame);

			JPanel formPanel = new JPanel(new GridLayout(18, 2, 6, 6));
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

			JTextField photoPathF = new JTextField();
			photoPathF.setEditable(false);
			JButton browsePhotoBtn = new JButton("Choose Photo...");
			JPanel photoPanel = new JPanel(new BorderLayout(5, 0));
			photoPanel.add(photoPathF, BorderLayout.CENTER);
			photoPanel.add(browsePhotoBtn, BorderLayout.EAST);

			browsePhotoBtn.addActionListener(e -> {
				try {
					JFileChooser chooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (JPG, PNG)", "jpg", "jpeg", "png");
					chooser.setFileFilter(filter);
					if (chooser.showOpenDialog(regDialog) == JFileChooser.APPROVE_OPTION) {
						photoPathF.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(regDialog, 
							"File chooser selection error: " + ex.getMessage(), 
							"File Error", JOptionPane.ERROR_MESSAGE);
				}
			});

			formPanel.add(new JLabel("Username:"));
			formPanel.add(usernameF);
			formPanel.add(new JLabel("Password:"));
			formPanel.add(passwordF);
			formPanel.add(new JLabel(""));
			formPanel.add(new JLabel(
					"<html><font size='2' color='gray'>Min 8 chars, upper/lowercase, number &amp; special char</font></html>"));
			formPanel.add(new JLabel("First Name:"));
			formPanel.add(firstNameF);
			formPanel.add(new JLabel("Last Name:"));
			formPanel.add(lastNameF);
			formPanel.add(new JLabel("Mobile Number (11 Digits):"));
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
				try {
					String user = usernameF.getText() != null ? usernameF.getText().trim() : "";
					String pass = passwordF.getPassword() != null ? new String(passwordF.getPassword()) : "";
					String firstName = firstNameF.getText() != null ? firstNameF.getText().trim() : "";
					String lastName = lastNameF.getText() != null ? lastNameF.getText().trim() : "";
					String mobile = mobileF.getText() != null ? mobileF.getText().trim() : "";
					String email = emailF.getText() != null ? emailF.getText().trim() : "";
					String batch = batchF.getText() != null ? batchF.getText().trim() : "";
					String nsuId = nsuIdF.getText() != null ? nsuIdF.getText().trim() : "";
					String dept = deptF.getText() != null ? deptF.getText().trim() : "";
					String major = majorF.getText() != null ? majorF.getText().trim() : "";
					String prof = profF.getText() != null ? profF.getText().trim() : "";
					String desig = desigF.getText() != null ? desigF.getText().trim() : "";
					String company = companyF.getText() != null ? companyF.getText().trim() : "";
					String country = countryF.getText() != null ? countryF.getText().trim() : "";
					String question = (String) secQuestionCombo.getSelectedItem();
					String answer = secAnswerF.getText() != null ? secAnswerF.getText().trim() : "";
					String photoPath = photoPathF.getText() != null ? photoPathF.getText().trim() : "";

					if (user.isEmpty() || pass.isEmpty() || firstName.isEmpty()) {
						status.setText("Username, Password, and First Name are required!");
						return;
					}

					String mobileError = accountManager.validateMobileNumber(mobile);
					if (mobileError != null) {
						status.setText(mobileError);
						return;
					}

					if (answer.isEmpty()) {
						status.setText("Security Answer is required!");
						return;
					}

					String passwordError = accountManager.validatePasswordStrength(pass);
					if (passwordError != null) {
						status.setText(passwordError);
						return;
					}

					String duplicateError = accountManager.checkDuplicates(user, nsuId, mobile, email);
					if (duplicateError != null) {
						status.setText(duplicateError);
						return;
					}

					boolean success = accountManager.registerUser(user, pass, firstName, lastName, mobile, email,
							batch, nsuId, dept, major, prof, desig, company, country, question, answer, photoPath);

					if (success) {
						JOptionPane.showMessageDialog(regDialog, "Registration Successful! You can now log in.",
								"Success", JOptionPane.INFORMATION_MESSAGE);
						regDialog.dispose();
					} else {
						status.setText("Failed to save registration data. Please try again.");
					}
				} catch (NullPointerException npe) {
					status.setText("Error reading form inputs. Please check entered values.");
					System.err.println("Null pointer error during registration parsing: " + npe.getMessage());
				} catch (Exception ex) {
					status.setText("An unexpected registration error occurred.");
					System.err.println("Registration Execution Error: " + ex.getMessage());
					JOptionPane.showMessageDialog(regDialog, "Error details: " + ex.getMessage(),
							"Registration Error", JOptionPane.ERROR_MESSAGE);
				}
			});

			JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
			bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
			bottomPanel.add(status, BorderLayout.NORTH);
			bottomPanel.add(submitBtn, BorderLayout.SOUTH);

			regDialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
			regDialog.add(bottomPanel, BorderLayout.SOUTH);

			regDialog.setVisible(true);
		} catch (Exception ex) {
			System.err.println("Error displaying registration dialog: " + ex.getMessage());
		}
	}
}