package javaswing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class MainDashboard extends JFrame {

	private String currentUsername;
	private CardLayout cardLayout;
	private JPanel mainContentPanel;

	// In-memory events data structure: {Title, Date, Location, Organizer,
	// Description}
	private List<String[]> eventsList = new ArrayList<>();

	public MainDashboard(String username) {
		this.currentUsername = username;

		// Seed sample events
		eventsList.add(new String[] { "Annual Alumni Reunion 2026", "2026-12-20 | 06:00 PM", "NSU Plaza", "Admin",
				"Join us for the grand annual reunion with networking, dinner, and cultural performances." });
		eventsList.add(new String[] { "Tech Career & Networking Fair", "2026-09-15 | 10:00 AM", "AUDI801", "Admin",
				"Meet top tech leaders, explore job opportunities, and get live interview feedback." });

		setTitle("NSU Alumni Hub - Welcome " + username);
		setSize(1020, 720);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// --- TOP HEADER BAR ---
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(new Color(24, 119, 242));
		headerPanel.setPreferredSize(new Dimension(getWidth(), 55));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

		JLabel logoLabel = new JLabel("NSU Alumni Hub");
		logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		logoLabel.setForeground(Color.WHITE);

		JLabel userLabel = new JLabel("Logged in as: " + username + "  ");
		userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
		userLabel.setForeground(Color.WHITE);

		JButton logoutBtn = new JButton("Logout");
		logoutBtn.setFocusPainted(false);
		logoutBtn.addActionListener(e -> {
			dispose();
			new GUI().guiexecute();
		});

		JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 12));
		rightHeader.setOpaque(false);
		rightHeader.add(userLabel);
		rightHeader.add(logoutBtn);

		headerPanel.add(logoLabel, BorderLayout.WEST);
		headerPanel.add(rightHeader, BorderLayout.EAST);
		add(headerPanel, BorderLayout.NORTH);

		// --- LEFT SIDEBAR NAVIGATION ---
		JPanel sidebar = new JPanel();
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		sidebar.setBackground(new Color(240, 242, 245));
		sidebar.setPreferredSize(new Dimension(200, getHeight()));
		sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

		JButton btnProfile = createNavButton("👤 My Profile");
		JButton btnFeed = createNavButton("📰 News Feed");
		JButton btnEvents = createNavButton("📅 Events");
		JButton btnMembers = createNavButton("👥 Alumni Directory");
		JButton btnMessages = createNavButton("💬 Messaging");
		JButton btnNotices = createNavButton("📌 Notice Board");
		JButton btnCvUpload = createNavButton("📄 CV / Resume Upload");
		JButton btnContactUs = createNavButton("📞 Contact Us");

		sidebar.add(Box.createVerticalStrut(15));
		sidebar.add(btnProfile);
		sidebar.add(btnFeed);
		sidebar.add(btnEvents);
		sidebar.add(btnMembers);
		sidebar.add(btnMessages);
		sidebar.add(btnNotices);
		sidebar.add(btnCvUpload);
		sidebar.add(btnContactUs);

		// ADMIN PANEL BUTTON (Visible only to Admin)
		boolean isAdmin = "Admin".equalsIgnoreCase(currentUsername.trim());
		JButton btnAdmin = null;
		if (isAdmin) {
			btnAdmin = createNavButton("⚙️ Admin Panel");
			btnAdmin.setBackground(new Color(220, 53, 69));
			btnAdmin.setForeground(Color.WHITE);
			sidebar.add(Box.createVerticalStrut(10));
			sidebar.add(btnAdmin);
		}

		add(sidebar, BorderLayout.WEST);

		// --- MAIN CONTENT AREA (CardLayout) ---
		cardLayout = new CardLayout();
		mainContentPanel = new JPanel(cardLayout);

		mainContentPanel.add(createProfilePanel(), "PROFILE");
		mainContentPanel.add(createFeedPanel(), "FEED");
		mainContentPanel.add(createEventsPanel(), "EVENTS");
		mainContentPanel.add(createMembersPanel(), "MEMBERS");
		mainContentPanel.add(createMessagingPanel(), "MESSAGES");
		mainContentPanel.add(createNoticeBoardPanel(), "NOTICES");
		mainContentPanel.add(createCvUploadPanel(), "CV_UPLOAD");
		mainContentPanel.add(createContactUsPanel(), "CONTACT_US");

		if (isAdmin) {
			mainContentPanel.add(createAdminPanel(), "ADMIN");
		}

		add(mainContentPanel, BorderLayout.CENTER);

		// Navigation Actions
		btnProfile.addActionListener(e -> cardLayout.show(mainContentPanel, "PROFILE"));
		btnFeed.addActionListener(e -> cardLayout.show(mainContentPanel, "FEED"));
		btnEvents.addActionListener(e -> cardLayout.show(mainContentPanel, "EVENTS"));
		btnMembers.addActionListener(e -> cardLayout.show(mainContentPanel, "MEMBERS"));
		btnMessages.addActionListener(e -> cardLayout.show(mainContentPanel, "MESSAGES"));
		btnNotices.addActionListener(e -> {
			mainContentPanel.add(createNoticeBoardPanel(), "NOTICES");
			cardLayout.show(mainContentPanel, "NOTICES");
		});
		btnCvUpload.addActionListener(e -> cardLayout.show(mainContentPanel, "CV_UPLOAD"));
		btnContactUs.addActionListener(e -> cardLayout.show(mainContentPanel, "CONTACT_US"));

		if (isAdmin && btnAdmin != null) {
			btnAdmin.addActionListener(e -> refreshAdminPanel());
		}
	}

	private JButton createNavButton(String text) {
		JButton btn = new JButton(text);
		btn.setMaximumSize(new Dimension(180, 40));
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		btn.setFocusPainted(false);
		btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
		btn.setMargin(new Insets(5, 10, 5, 10));
		return btn;
	}

	private void refreshAdminPanel() {
		mainContentPanel.add(createAdminPanel(), "ADMIN");
		cardLayout.show(mainContentPanel, "ADMIN");
	}

	// ==========================================
	// ⚙️ EXCLUSIVE ADMIN PANEL
	// ==========================================
	private JPanel createAdminPanel() {
		JTabbedPane adminTabs = new JTabbedPane();
		adminTabs.setFont(new Font("SansSerif", Font.BOLD, 13));

		adminTabs.addTab("Manage Events", createAdminEventsTab());
		adminTabs.addTab("Manage Users", createAdminUsersTab());
		adminTabs.addTab("Moderate Feed Posts", createAdminPostsTab());
		adminTabs.addTab("Manage Notices", createAdminNoticesTab());

		JPanel container = new JPanel(new BorderLayout());
		container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container.add(adminTabs, BorderLayout.CENTER);

		return container;
	}

	// --- Admin Sub-Tab 1: Manage Events ---
	private JPanel createAdminEventsTab() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String[] columns = { "Index", "Title", "Date & Time", "Location", "Organizer" };
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		for (int i = 0; i < eventsList.size(); i++) {
			String[] ev = eventsList.get(i);
			model.addRow(new Object[] { i, ev[0], ev[1], ev[2], ev[3] });
		}

		JTable table = new JTable(model);
		table.setRowHeight(28);

		JButton editBtn = new JButton("Edit Selected Event");
		JButton deleteBtn = new JButton("Delete Selected Event");

		editBtn.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				showEditEventDialog(selectedRow, panel);
			} else {
				JOptionPane.showMessageDialog(panel, "Please select an event to edit.");
			}
		});

		deleteBtn.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete this event?",
						"Confirm Delete", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					eventsList.remove(selectedRow);
					JOptionPane.showMessageDialog(panel, "Event deleted successfully.");
					refreshAdminPanel();
				}
			} else {
				JOptionPane.showMessageDialog(panel, "Please select an event to delete.");
			}
		});

		JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnBar.add(editBtn);
		btnBar.add(deleteBtn);

		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(btnBar, BorderLayout.SOUTH);
		return panel;
	}

	private void showEditEventDialog(int index, JPanel parent) {
		String[] event = eventsList.get(index);
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Edit Event",
				Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setSize(400, 380);
		dialog.setLocationRelativeTo(parent);
		dialog.setLayout(new BorderLayout(10, 10));

		JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
		form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JTextField titleF = new JTextField(event[0]);
		JTextField dateF = new JTextField(event[1]);
		JTextField locF = new JTextField(event[2]);
		JTextArea descF = new JTextArea(event.length > 4 ? event[4] : "", 3, 20);

		form.add(new JLabel("Title:"));
		form.add(titleF);
		form.add(new JLabel("Date & Time:"));
		form.add(dateF);
		form.add(new JLabel("Location:"));
		form.add(locF);
		form.add(new JLabel("Description:"));
		form.add(new JScrollPane(descF));

		JButton saveBtn = new JButton("Save Changes");
		saveBtn.addActionListener(e -> {
			eventsList.set(index, new String[] { titleF.getText().trim(), dateF.getText().trim(), locF.getText().trim(),
					event[3], descF.getText().trim() });
			JOptionPane.showMessageDialog(dialog, "Event updated successfully!");
			dialog.dispose();
			mainContentPanel.add(createEventsPanel(), "EVENTS");
			if ("Admin".equalsIgnoreCase(currentUsername)) {
				refreshAdminPanel();
			}
		});

		dialog.add(form, BorderLayout.CENTER);
		dialog.add(saveBtn, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	// --- Admin Sub-Tab 2: Manage Registered Users ---
	private JPanel createAdminUsersTab() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String[] columns = { "Username", "Full Name", "Email", "NSU ID", "Department" };
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		File file = new File("alumni_users.csv");
		if (file.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				boolean header = true;
				while ((line = br.readLine()) != null) {
					if (header) {
						header = false;
						continue;
					}
					String[] parts = line.split(",", -1);
					if (parts.length >= 8) {
						String name = (parts.length >= 4) ? parts[2].trim() + " " + parts[3].trim() : "";
						model.addRow(new Object[] { parts[0].trim(), name, parts[5].trim(), parts[7].trim(),
								parts.length >= 9 ? parts[8].trim() : "" });
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		JTable table = new JTable(model);
		table.setRowHeight(25);

		JButton deleteUserBtn = new JButton("Delete Registered User");
		deleteUserBtn.setBackground(new Color(220, 53, 69));
		deleteUserBtn.setForeground(Color.WHITE);

		deleteUserBtn.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				String targetUser = (String) model.getValueAt(selectedRow, 0);

				if ("Admin".equalsIgnoreCase(targetUser)) {
					JOptionPane.showMessageDialog(panel, "Cannot delete the System Admin user account!",
							"Action Denied", JOptionPane.ERROR_MESSAGE);
					return;
				}

				int confirm = JOptionPane.showConfirmDialog(panel,
						"Are you sure you want to permanently delete user '" + targetUser + "'?",
						"Confirm User Deletion", JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					deleteUserFromCSV(targetUser);
					JOptionPane.showMessageDialog(panel, "User '" + targetUser + "' deleted successfully!");
					refreshAdminPanel();
				}
			} else {
				JOptionPane.showMessageDialog(panel, "Please select a user to delete.");
			}
		});

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		footer.add(deleteUserBtn);

		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(footer, BorderLayout.SOUTH);
		return panel;
	}

	private void deleteUserFromCSV(String usernameToDelete) {
		File inputFile = new File("alumni_users.csv");
		File tempFile = new File("alumni_users_temp.csv");

		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				String[] parts = currentLine.split(",", -1);
				if (parts[0].trim().equalsIgnoreCase(usernameToDelete.trim())) {
					continue;
				}
				writer.write(currentLine + System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (inputFile.delete()) {
			tempFile.renameTo(inputFile);
		}
	}

	// --- Admin Sub-Tab 3: Moderate Newsfeed Posts ---
	private JPanel createAdminPostsTab() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String[] columns = { "Post ID", "Author", "Content Snippet", "Timestamp" };
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		List<String[]> posts = FeedManager.loadAllPosts();
		for (String[] p : posts) {
			String pId = p[0];
			String author = p[1];
			String text = p[2].length() > 40 ? p[2].substring(0, 40) + "..." : p[2];
			String time = p.length > 4 ? p[4] : "";
			model.addRow(new Object[] { pId, author, text, time });
		}

		JTable table = new JTable(model);
		table.setRowHeight(25);

		JButton deletePostBtn = new JButton("Delete Post");
		deletePostBtn.addActionListener(e -> {
			int selRow = table.getSelectedRow();
			if (selRow != -1) {
				String postId = (String) model.getValueAt(selRow, 0);
				int confirm = JOptionPane.showConfirmDialog(panel, "Delete selected post and its comments?", "Confirm",
						JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					FeedManager.deletePost(postId);
					JOptionPane.showMessageDialog(panel, "Post removed.");
					refreshAdminPanel();
				}
			} else {
				JOptionPane.showMessageDialog(panel, "Please select a post.");
			}
		});

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		footer.add(deletePostBtn);

		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(footer, BorderLayout.SOUTH);
		return panel;
	}

	// --- Admin Sub-Tab 4: Manage Notices ---
	private JPanel createAdminNoticesTab() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		String[] columns = { "Notice ID", "Notice Content", "Posted By", "Date & Time" };
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};

		List<String[]> notices = NoticeManager.loadAllNotices();
		for (String[] n : notices) {
			model.addRow(new Object[] { n[0], n[1], n[2], n.length > 3 ? n[3] : "" });
		}

		JTable table = new JTable(model);
		table.setRowHeight(28);

		JButton addBtn = new JButton("➕ Post Notice");
		JButton editBtn = new JButton("✏️ Edit Notice");
		JButton deleteBtn = new JButton("🗑️ Delete Notice");
		deleteBtn.setBackground(new Color(220, 53, 69));
		deleteBtn.setForeground(Color.WHITE);

		addBtn.addActionListener(e -> {
			String newNotice = JOptionPane.showInputDialog(panel, "Enter new notice text:", "Post Official Notice",
					JOptionPane.PLAIN_MESSAGE);
			if (newNotice != null && !newNotice.trim().isEmpty()) {
				NoticeManager.saveNotice(newNotice.trim(), currentUsername);
				JOptionPane.showMessageDialog(panel, "Notice posted successfully!");
				refreshAdminPanel();
			}
		});

		editBtn.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				String noticeId = (String) model.getValueAt(selectedRow, 0);
				String currentContent = (String) model.getValueAt(selectedRow, 1);

				String updatedNotice = JOptionPane.showInputDialog(panel, "Edit notice text:", currentContent);
				if (updatedNotice != null && !updatedNotice.trim().isEmpty()) {
					NoticeManager.editNotice(noticeId, updatedNotice.trim());
					JOptionPane.showMessageDialog(panel, "Notice updated successfully!");
					refreshAdminPanel();
				}
			} else {
				JOptionPane.showMessageDialog(panel, "Please select a notice to edit.");
			}
		});

		deleteBtn.addActionListener(e -> {
			int selectedRow = table.getSelectedRow();
			if (selectedRow != -1) {
				String noticeId = (String) model.getValueAt(selectedRow, 0);
				int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete this notice?",
						"Confirm Delete", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					NoticeManager.deleteNotice(noticeId);
					JOptionPane.showMessageDialog(panel, "Notice deleted.");
					refreshAdminPanel();
				}
			} else {
				JOptionPane.showMessageDialog(panel, "Please select a notice to delete.");
			}
		});

		JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
		btnBar.add(addBtn);
		btnBar.add(editBtn);
		btnBar.add(deleteBtn);

		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(btnBar, BorderLayout.SOUTH);

		return panel;
	}

	// 0. Own Profile Panel
	// 0. Own Profile Panel with Photo Upload Option
	private JPanel createProfilePanel() {
		JPanel outerPanel = new JPanel(new BorderLayout(15, 15));
		outerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		String[] userRecord = accountManager.getUserProfile(currentUsername);

		JPanel cardPanel = new JPanel(new BorderLayout());
		cardPanel.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
						BorderFactory.createEmptyBorder(0, 0, 20, 0)));

		JPanel headerPanel = new JPanel();
		headerPanel.setBackground(new Color(24, 43, 73));
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

		String fullName = (userRecord != null && userRecord.length >= 4)
				? (userRecord[2].trim() + " " + userRecord[3].trim())
				: currentUsername;

		String profession = (userRecord != null && userRecord.length >= 12)
				? (userRecord[10].trim() + " • " + userRecord[11].trim())
				: "Alumnus";

		// Check if user has uploaded a custom profile photo
		String photoPath = (userRecord != null && userRecord.length >= 17) ? userRecord[16].trim() : "";
		JLabel avatarLabel = createAvatarLabel(photoPath, 100, 100);
		avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JButton uploadPhotoBtn = new JButton("📷 Upload Photo");
		uploadPhotoBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
		uploadPhotoBtn.setFocusPainted(false);
		uploadPhotoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

		uploadPhotoBtn.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (JPG, PNG)", "jpg", "jpeg",
					"png");
			chooser.setFileFilter(filter);

			if (chooser.showOpenDialog(outerPanel) == JFileChooser.APPROVE_OPTION) {
				File sourceFile = chooser.getSelectedFile();
				try {
					File targetDir = new File("profile_pics");
					if (!targetDir.exists()) {
						targetDir.mkdirs();
					}

					String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf("."));
					File targetFile = new File(targetDir, currentUsername + "_profile" + extension);

					Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

					if (accountManager.updateProfilePhoto(currentUsername, targetFile.getPath())) {
						JOptionPane.showMessageDialog(outerPanel, "Profile photo updated!");
						mainContentPanel.add(createProfilePanel(), "PROFILE");
						cardLayout.show(mainContentPanel, "PROFILE");
					}
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(outerPanel, "Failed to upload image: " + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JLabel nameLabel = new JLabel(fullName);
		nameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel subLabel = new JLabel(profession);
		subLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
		subLabel.setForeground(new Color(200, 210, 225));
		subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		headerPanel.add(avatarLabel);
		headerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
		headerPanel.add(uploadPhotoBtn);
		headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		headerPanel.add(nameLabel);
		headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		headerPanel.add(subLabel);

		JPanel bodyPanel = new JPanel(new GridLayout(8, 2, 15, 12));
		bodyPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 20, 40));

		if (userRecord != null) {
			addDetailRow(bodyPanel, "Username:", userRecord[0].trim());
			addDetailRow(bodyPanel, "NSU ID:", userRecord[7].trim());
			addDetailRow(bodyPanel, "Department:", userRecord[8].trim());
			addDetailRow(bodyPanel, "Major:", userRecord[9].trim());
			addDetailRow(bodyPanel, "Batch:", userRecord[6].trim());
			addDetailRow(bodyPanel, "Country:", userRecord[13].trim());
			addDetailRow(bodyPanel, "Email:", userRecord[5].trim());
			addDetailRow(bodyPanel, "Mobile:", userRecord[4].trim());
		}

		cardPanel.add(headerPanel, BorderLayout.NORTH);
		cardPanel.add(bodyPanel, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(cardPanel);
		scrollPane.setBorder(null);

		outerPanel.add(scrollPane, BorderLayout.CENTER);
		return outerPanel;
	}

	private JLabel createAvatarLabel(String imagePath, int width, int height) {
		JLabel avatarLabel = new JLabel();
		if (imagePath != null && !imagePath.isEmpty() && new File(imagePath).exists()) {
			ImageIcon icon = new ImageIcon(imagePath);
			Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
			avatarLabel.setIcon(new ImageIcon(img));
		} else {
			avatarLabel.setText("👤");
			avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, height / 2));
		}
		return avatarLabel;
	}

	// 1. News Feed Panel
	private JPanel createFeedPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel createPostPanel = new JPanel(new BorderLayout(5, 5));
		createPostPanel.setBorder(BorderFactory.createTitledBorder("Create a Post"));
		JTextArea postInput = new JTextArea(3, 30);
		postInput.setLineWrap(true);
		postInput.setWrapStyleWord(true);
		JButton postBtn = new JButton("Post");

		createPostPanel.add(new JScrollPane(postInput), BorderLayout.CENTER);
		createPostPanel.add(postBtn, BorderLayout.EAST);

		JPanel feedList = new JPanel();
		feedList.setLayout(new BoxLayout(feedList, BoxLayout.Y_AXIS));

		Runnable reloadFeed = () -> {
			feedList.removeAll();
			List<String[]> savedPosts = FeedManager.loadAllPosts();
			for (int i = savedPosts.size() - 1; i >= 0; i--) {
				String[] post = savedPosts.get(i);
				String postId = post[0];
				String author = post[1];
				String content = post[2];
				int likes = post.length > 3 ? Integer.parseInt(post[3]) : 0;
				String timestamp = post.length > 4 ? post[4] : "";

				feedList.add(createPostWidget(postId, author, content, likes, timestamp, () -> {
					feedList.removeAll();
					List<String[]> updatedPosts = FeedManager.loadAllPosts();
					for (int j = updatedPosts.size() - 1; j >= 0; j--) {
						String[] p = updatedPosts.get(j);
						String pId = p[0];
						String pAuthor = p[1];
						String pContent = p[2];
						int pLikes = p.length > 3 ? Integer.parseInt(p[3]) : 0;
						String pTimestamp = p.length > 4 ? p[4] : "";

						feedList.add(createPostWidget(pId, pAuthor, pContent, pLikes, pTimestamp, null));
						feedList.add(Box.createVerticalStrut(12));
					}
					feedList.revalidate();
					feedList.repaint();
				}));
				feedList.add(Box.createVerticalStrut(12));
			}
			feedList.revalidate();
			feedList.repaint();
		};

		reloadFeed.run();

		postBtn.addActionListener(e -> {
			String content = postInput.getText().trim();
			if (!content.isEmpty()) {
				if (FeedManager.savePost(currentUsername, content)) {
					postInput.setText("");
					reloadFeed.run();
				} else {
					JOptionPane.showMessageDialog(panel, "Error saving post.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(feedList);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		panel.add(createPostPanel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private void renderCommentTree(String postId, String parentId, List<String[]> allComments, JPanel container,
			int indentLevel, Runnable refreshCallback) {
		for (String[] c : allComments) {
			String cId = c[0];
			String cAuthor = c[1];
			String cText = c[2];
			String cParent = c.length > 3 ? c[3] : "null";
			String cTimestamp = c.length > 4 ? c[4] : "";

			if (cParent.equals(parentId)) {
				JPanel commentRow = new JPanel(new BorderLayout(5, 2));
				commentRow.setOpaque(false);
				commentRow.setBorder(BorderFactory.createEmptyBorder(2, Math.min(indentLevel * 15, 60), 2, 0));

				JLabel commentLabel = new JLabel("<html><b>" + cAuthor + "</b> "
						+ (!cTimestamp.isEmpty() ? "<font color='#888888' size='2'>(" + cTimestamp + ")</font>" : "")
						+ ": " + cText + "</html>");
				commentLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

				JButton replyBtn = new JButton("Reply");
				replyBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
				replyBtn.setBorderPainted(false);
				replyBtn.setContentAreaFilled(false);
				replyBtn.setFocusPainted(false);

				replyBtn.addActionListener(e -> {
					String replyText = JOptionPane.showInputDialog(container, "Reply to " + cAuthor + ":");
					if (replyText != null && !replyText.trim().isEmpty()) {
						FeedManager.saveComment(postId, currentUsername, replyText.trim(), cId);
						refreshCallback.run();
					}
				});

				JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
				actions.setOpaque(false);
				actions.add(replyBtn);

				if (cAuthor.equals(currentUsername) || "Admin".equalsIgnoreCase(currentUsername)) {
					JButton cMenuBtn = new JButton("⋮");
					cMenuBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
					cMenuBtn.setBorderPainted(false);
					cMenuBtn.setContentAreaFilled(false);

					JPopupMenu cMenu = new JPopupMenu();
					JMenuItem editC = new JMenuItem("Edit");
					JMenuItem deleteC = new JMenuItem("Delete");
					cMenu.add(editC);
					cMenu.add(deleteC);

					cMenuBtn.addActionListener(e -> cMenu.show(cMenuBtn, 0, cMenuBtn.getHeight()));

					editC.addActionListener(e -> {
						String updatedText = JOptionPane.showInputDialog(container, "Edit comment:", cText);
						if (updatedText != null && !updatedText.trim().isEmpty()) {
							FeedManager.editComment(cId, updatedText.trim());
							refreshCallback.run();
						}
					});

					deleteC.addActionListener(e -> {
						FeedManager.deleteComment(cId);
						refreshCallback.run();
					});

					actions.add(cMenuBtn);
				}

				commentRow.add(commentLabel, BorderLayout.CENTER);
				commentRow.add(actions, BorderLayout.EAST);

				container.add(commentRow);

				renderCommentTree(postId, cId, allComments, container, indentLevel + 1, refreshCallback);
			}
		}
	}

	private JPanel createPostWidget(String postId, String author, String text, int initialLikes, String timestamp,
			Runnable onDeleteCallback) {
		JPanel postCard = new JPanel(new BorderLayout(8, 8));
		postCard.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
						BorderFactory.createEmptyBorder(12, 12, 12, 12)));
		postCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 800));

		JPanel topHeader = new JPanel(new BorderLayout());
		topHeader.setOpaque(false);

		JLabel authorLabel = new JLabel("<html>👤 <b>" + author + "</b> "
				+ (!timestamp.isEmpty() ? "<font color='#777777'> • " + timestamp + "</font>" : "") + "</html>");
		authorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		authorLabel.setForeground(new Color(24, 119, 242));
		topHeader.add(authorLabel, BorderLayout.WEST);

		if (author.equals(currentUsername) || "Admin".equalsIgnoreCase(currentUsername)) {
			JButton menuBtn = new JButton("⋮");
			menuBtn.setBorderPainted(false);
			menuBtn.setContentAreaFilled(false);
			menuBtn.setFocusPainted(false);

			JPopupMenu menu = new JPopupMenu();
			JMenuItem editItem = new JMenuItem("Edit Post");
			JMenuItem deleteItem = new JMenuItem("Delete Post");
			menu.add(editItem);
			menu.add(deleteItem);

			menuBtn.addActionListener(e -> menu.show(menuBtn, 0, menuBtn.getHeight()));

			editItem.addActionListener(e -> {
				String newText = JOptionPane.showInputDialog(postCard, "Edit your post:", text);
				if (newText != null && !newText.trim().isEmpty()) {
					FeedManager.editPost(postId, newText.trim());
					if (onDeleteCallback != null) {
						onDeleteCallback.run();
					}
				}
			});

			deleteItem.addActionListener(e -> {
				int choice = JOptionPane.showConfirmDialog(postCard, "Are you sure you want to delete this post?",
						"Delete Post", JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					FeedManager.deletePost(postId);
					if (onDeleteCallback != null) {
						onDeleteCallback.run();
					}
				}
			});

			topHeader.add(menuBtn, BorderLayout.EAST);
		}

		JTextArea bodyText = new JTextArea(text);
		bodyText.setFont(new Font("SansSerif", Font.PLAIN, 13));
		bodyText.setLineWrap(true);
		bodyText.setWrapStyleWord(true);
		bodyText.setEditable(false);
		bodyText.setOpaque(false);

		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		actionPanel.setOpaque(false);

		boolean likedByMe = FeedManager.hasUserLikedPost(postId, currentUsername);
		JButton likeBtn = new JButton((likedByMe ? "❤️ Liked (" : "🤍 Like (") + initialLikes + ")");
		likeBtn.setFocusPainted(false);

		likeBtn.addActionListener(e -> {
			boolean isLiked = FeedManager.hasUserLikedPost(postId, currentUsername);
			FeedManager.toggleLikePost(postId, currentUsername, !isLiked);
			int newLikes = isLiked ? initialLikes - 1 : initialLikes + 1;
			likeBtn.setText((!isLiked ? "❤️ Liked (" : "🤍 Like (") + newLikes + ")");
		});

		actionPanel.add(likeBtn);

		JPanel commentsPanel = new JPanel();
		commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
		commentsPanel.setOpaque(false);

		Runnable refreshComments = new Runnable() {
			@Override
			public void run() {
				commentsPanel.removeAll();
				List<String[]> allComments = FeedManager.loadCommentsForPost(postId);
				renderCommentTree(postId, "null", allComments, commentsPanel, 0, this);
				commentsPanel.revalidate();
				commentsPanel.repaint();
			}
		};
		refreshComments.run();

		JPanel newCommentPanel = new JPanel(new BorderLayout(5, 5));
		newCommentPanel.setOpaque(false);

		JTextField commentInput = new JTextField();
		JButton addCommentBtn = new JButton("Comment");

		addCommentBtn.addActionListener(e -> {
			String cText = commentInput.getText().trim();
			if (!cText.isEmpty()) {
				FeedManager.saveComment(postId, currentUsername, cText, "null");
				commentInput.setText("");
				refreshComments.run();
			}
		});

		newCommentPanel.add(commentInput, BorderLayout.CENTER);
		newCommentPanel.add(addCommentBtn, BorderLayout.EAST);

		JPanel centerArea = new JPanel(new BorderLayout(5, 5));
		centerArea.setOpaque(false);
		centerArea.add(bodyText, BorderLayout.NORTH);
		centerArea.add(actionPanel, BorderLayout.SOUTH);

		JPanel bottomArea = new JPanel(new BorderLayout(5, 5));
		bottomArea.setOpaque(false);
		bottomArea.add(commentsPanel, BorderLayout.CENTER);
		bottomArea.add(newCommentPanel, BorderLayout.SOUTH);

		postCard.add(topHeader, BorderLayout.NORTH);
		postCard.add(centerArea, BorderLayout.CENTER);
		postCard.add(bottomArea, BorderLayout.SOUTH);

		return postCard;
	}

	// 2. Events Panel (Accessible by Anyone to View/Post)
	private JPanel createEventsPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel headerPanel = new JPanel(new BorderLayout());
		JLabel title = new JLabel("📅 Upcoming Alumni Events (Click row for details/actions)");
		title.setFont(new Font("SansSerif", Font.BOLD, 16));
		headerPanel.add(title, BorderLayout.WEST);

		JButton postEventBtn = new JButton("➕ Post Event");
		postEventBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
		postEventBtn.setFocusPainted(false);
		postEventBtn.addActionListener(e -> showPostEventDialog(panel));

		headerPanel.add(postEventBtn, BorderLayout.EAST);
		panel.add(headerPanel, BorderLayout.NORTH);

		String[] columns = { "Event Title", "Date & Time", "Location", "Organizer" };
		DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(30);

		Runnable refreshEventsTable = () -> {
			tableModel.setRowCount(0);
			for (String[] ev : eventsList) {
				tableModel.addRow(new Object[] { ev[0], ev[1], ev[2], ev[3] });
			}
		};
		refreshEventsTable.run();

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow != -1 && selectedRow < eventsList.size()) {
					String[] eventData = eventsList.get(selectedRow);
					Window parentWindow = SwingUtilities.getWindowAncestor(panel);
					showEventDetailsPopup(parentWindow, eventData, selectedRow);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(table);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private void showPostEventDialog(JPanel parentPanel) {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parentPanel), "Post New Event",
				Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setSize(400, 380);
		dialog.setLocationRelativeTo(parentPanel);
		dialog.setLayout(new BorderLayout(10, 10));

		JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
		formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JTextField titleField = new JTextField();
		JTextField dateField = new JTextField("2026-YYYY-DD | 00:00 AM");
		JTextField locationField = new JTextField();
		JTextArea descArea = new JTextArea(3, 20);
		descArea.setLineWrap(true);

		formPanel.add(new JLabel("Event Title:"));
		formPanel.add(titleField);
		formPanel.add(new JLabel("Date & Time:"));
		formPanel.add(dateField);
		formPanel.add(new JLabel("Location:"));
		formPanel.add(locationField);
		formPanel.add(new JLabel("Description:"));
		formPanel.add(new JScrollPane(descArea));

		JButton submitBtn = new JButton("Submit Event");
		submitBtn.addActionListener(e -> {
			String t = titleField.getText().trim();
			String d = dateField.getText().trim();
			String l = locationField.getText().trim();
			String desc = descArea.getText().trim();

			if (!t.isEmpty() && !d.isEmpty() && !l.isEmpty()) {
				eventsList.add(0, new String[] { t, d, l, currentUsername, desc });
				JOptionPane.showMessageDialog(dialog, "Event posted successfully!");
				dialog.dispose();

				mainContentPanel.add(createEventsPanel(), "EVENTS");
				cardLayout.show(mainContentPanel, "EVENTS");
			} else {
				JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		});

		dialog.add(formPanel, BorderLayout.CENTER);
		dialog.add(submitBtn, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	private void showEventDetailsPopup(Window parentWindow, String[] event, int eventIndex) {
		JDialog dialog = new JDialog(parentWindow, "Event Details", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setSize(480, 400);
		dialog.setLocationRelativeTo(parentWindow);
		dialog.setLayout(new BorderLayout());

		JPanel header = new JPanel();
		header.setBackground(new Color(24, 119, 242));
		header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JLabel titleLbl = new JLabel(event[0]);
		titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
		titleLbl.setForeground(Color.WHITE);
		header.add(titleLbl);

		JPanel body = new JPanel(new GridLayout(3, 2, 10, 10));
		body.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

		addDetailRow(body, "📅 Date & Time:", event[1]);
		addDetailRow(body, "📍 Location:", event[2]);
		addDetailRow(body, "👤 Organizer:", event[3]);

		JTextArea descArea = new JTextArea(
				"📝 Description:\n" + (event.length > 4 ? event[4] : "No details provided."));
		descArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
		descArea.setEditable(false);
		descArea.setLineWrap(true);
		descArea.setWrapStyleWord(true);
		descArea.setBackground(new Color(245, 247, 250));
		descArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
		centerPanel.add(body, BorderLayout.NORTH);
		centerPanel.add(new JScrollPane(descArea), BorderLayout.CENTER);

		JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));

		boolean canModify = event[3].equalsIgnoreCase(currentUsername)
				|| "Admin".equalsIgnoreCase(currentUsername.trim());

		if (canModify) {
			JButton editBtn = new JButton("✏️ Edit");
			JButton deleteBtn = new JButton("🗑️ Delete");
			deleteBtn.setBackground(new Color(220, 53, 69));
			deleteBtn.setForeground(Color.WHITE);

			editBtn.addActionListener(e -> {
				dialog.dispose();
				showEditEventDialog(eventIndex, (JPanel) mainContentPanel.getComponent(0));
			});

			deleteBtn.addActionListener(e -> {
				int confirm = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete this event?",
						"Confirm Delete", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					eventsList.remove(eventIndex);
					JOptionPane.showMessageDialog(dialog, "Event deleted.");
					dialog.dispose();
					mainContentPanel.add(createEventsPanel(), "EVENTS");
					cardLayout.show(mainContentPanel, "EVENTS");
				}
			});

			footer.add(editBtn);
			footer.add(deleteBtn);
		}

		JButton closeBtn = new JButton("Close");
		closeBtn.addActionListener(e -> dialog.dispose());
		footer.add(closeBtn);

		dialog.add(header, BorderLayout.NORTH);
		dialog.add(centerPanel, BorderLayout.CENTER);
		dialog.add(footer, BorderLayout.SOUTH);

		dialog.setVisible(true);
	}

	// 3. Alumni Members Directory
	private JPanel createMembersPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JLabel title = new JLabel("Registered Alumni Directory (Click a row to preview profile)");
		title.setFont(new Font("SansSerif", Font.BOLD, 16));

		String[] columns = { "Name", "Department", "Major", "Batch", "Profession", "Country" };

		List<String[]> fullRecords = new ArrayList<>();
		List<Object[]> tableDataList = new ArrayList<>();
		File file = new File("alumni_users.csv");

		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line;
				boolean isFirstLine = true;

				while ((line = reader.readLine()) != null) {
					if (isFirstLine) {
						isFirstLine = false;
						continue;
					}

					String[] parts = line.split(",", -1);
					if (parts.length >= 13) {
						fullRecords.add(parts);

						String fullName = parts[2].trim() + " " + parts[3].trim();
						tableDataList.add(new Object[] { fullName, parts[8].trim(), parts[9].trim(), parts[6].trim(),
								parts[10].trim(), parts[12].trim() });
					}
				}
			} catch (IOException e) {
				System.err.println("Error reading alumni directory: " + e.getMessage());
			}
		}

		Object[][] data = tableDataList.toArray(new Object[0][]);

		DefaultTableModel model = new DefaultTableModel(data, columns) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(25);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow != -1 && selectedRow < fullRecords.size()) {
					String[] record = fullRecords.get(selectedRow);
					Window parentWindow = SwingUtilities.getWindowAncestor(panel);
					showProfilePopup(parentWindow, record);
				}
			}
		});

		JScrollPane tableScroll = new JScrollPane(table);

		panel.add(title, BorderLayout.NORTH);
		panel.add(tableScroll, BorderLayout.CENTER);
		return panel;
	}

	private void showProfilePopup(Window parentWindow, String[] user) {
		JDialog dialog = new JDialog(parentWindow, "Alumnus Profile Preview", Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setSize(420, 520);
		dialog.setLocationRelativeTo(parentWindow);
		dialog.setLayout(new BorderLayout());

		JPanel headerPanel = new JPanel();
		headerPanel.setBackground(new Color(24, 43, 73));
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		String photoPath = (user.length >= 17) ? user[16].trim() : "";
		JLabel avatarLabel = createAvatarLabel(photoPath, 80, 80);
		avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel nameLabel = new JLabel(user[2].trim() + " " + user[3].trim());
		nameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JLabel subLabel = new JLabel(user[10].trim() + " • " + user[11].trim());
		subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		subLabel.setForeground(new Color(200, 210, 225));
		subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		headerPanel.add(avatarLabel);
		headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
		headerPanel.add(nameLabel);
		headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		headerPanel.add(subLabel);

		JPanel bodyPanel = new JPanel(new GridLayout(7, 2, 10, 10));
		bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

		addDetailRow(bodyPanel, "NSU ID:", user[7].trim());
		addDetailRow(bodyPanel, "Department:", user[8].trim());
		addDetailRow(bodyPanel, "Major:", user[9].trim());
		addDetailRow(bodyPanel, "Batch:", user[6].trim());
		addDetailRow(bodyPanel, "Country:", user[12].trim());
		addDetailRow(bodyPanel, "Email:", user[5].trim());
		addDetailRow(bodyPanel, "Mobile:", user[4].trim());

		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));

		JButton closeBtn = new JButton("Close");
		closeBtn.setFocusPainted(false);
		closeBtn.addActionListener(e -> dialog.dispose());
		footerPanel.add(closeBtn);

		dialog.add(headerPanel, BorderLayout.NORTH);
		dialog.add(bodyPanel, BorderLayout.CENTER);
		dialog.add(footerPanel, BorderLayout.SOUTH);

		dialog.setResizable(false);
		dialog.setVisible(true);
	}

	private void addDetailRow(JPanel container, String title, String value) {
		JLabel lblTitle = new JLabel(title);
		lblTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblTitle.setForeground(new Color(100, 100, 100));

		JLabel lblValue = new JLabel(value.isEmpty() ? "N/A" : value);
		lblValue.setFont(new Font("SansSerif", Font.PLAIN, 13));

		container.add(lblTitle);
		container.add(lblValue);
	}

	// 4. Messaging Panel
	private JPanel createMessagingPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 10, 10, 10);

		JLabel infoLabel = new JLabel("Connect with Alumni Live via Socket Chatroom");
		infoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

		JButton openChatBtn = new JButton("Launch Live Alumni Chatroom");
		openChatBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
		openChatBtn.setPreferredSize(new Dimension(260, 45));

		openChatBtn.addActionListener(e -> {
			chatbox chat = new chatbox(currentUsername, null);
			
			chat.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			chat.setVisible(true);
		});

		panel.add(infoLabel, gbc);
		gbc.gridy = 1;
		panel.add(openChatBtn, gbc);

		return panel;
	}

	// 5. Notice Board Panel
	private JPanel createNoticeBoardPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel header = new JPanel(new BorderLayout());
		JLabel title = new JLabel("📌 Official Alumni Notices");
		title.setFont(new Font("SansSerif", Font.BOLD, 16));
		header.add(title, BorderLayout.WEST);

		if ("Admin".equalsIgnoreCase(currentUsername.trim())) {
			JButton postNoticeBtn = new JButton("➕ Add Notice");
			postNoticeBtn.setFocusPainted(false);
			postNoticeBtn.addActionListener(e -> {
				String newNotice = JOptionPane.showInputDialog(panel, "Enter notice content:", "Post Notice",
						JOptionPane.PLAIN_MESSAGE);
				if (newNotice != null && !newNotice.trim().isEmpty()) {
					NoticeManager.saveNotice(newNotice.trim(), currentUsername);
					JOptionPane.showMessageDialog(panel, "Notice published!");
					mainContentPanel.add(createNoticeBoardPanel(), "NOTICES");
					cardLayout.show(mainContentPanel, "NOTICES");
				}
			});
			header.add(postNoticeBtn, BorderLayout.EAST);
		}

		DefaultListModel<String> listModel = new DefaultListModel<>();
		List<String[]> notices = NoticeManager.loadAllNotices();
		for (int i = notices.size() - 1; i >= 0; i--) {
			String[] n = notices.get(i);
			String text = n[1];
			String time = n.length > 3 ? " [" + n[3] + "]" : "";
			listModel.addElement(text + time);
		}

		JList<String> noticeList = new JList<>(listModel);
		noticeList.setFont(new Font("SansSerif", Font.PLAIN, 14));
		noticeList.setFixedCellHeight(35);

		panel.add(header, BorderLayout.NORTH);
		panel.add(new JScrollPane(noticeList), BorderLayout.CENTER);

		return panel;
	}

	// 6. CV / Resume Upload Panel
	private JPanel createCvUploadPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(8, 8, 8, 8);

		JLabel title = new JLabel("📄 Upload Your CV / Resume (PDF / DOCX)");
		title.setFont(new Font("SansSerif", Font.BOLD, 16));

		JLabel fileStatusLabel = new JLabel("No file selected.");
		fileStatusLabel.setForeground(Color.GRAY);

		JButton selectFileBtn = new JButton("Browse File");
		JButton uploadBtn = new JButton("Submit CV");
		uploadBtn.setEnabled(false);

		final File[] selectedFile = new File[1];

		selectFileBtn.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF & Word Documents", "pdf", "docx");
			chooser.setFileFilter(filter);

			int returnVal = chooser.showOpenDialog(panel);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				selectedFile[0] = chooser.getSelectedFile();
				fileStatusLabel.setText("Selected: " + selectedFile[0].getName());
				fileStatusLabel.setForeground(new Color(0, 120, 0));
				uploadBtn.setEnabled(true);
			}
		});

		uploadBtn.addActionListener(e -> {
			if (selectedFile[0] != null) {
				try {
					File targetDir = new File("cv");
					if (!targetDir.exists()) {
						targetDir.mkdirs();
					}

					File targetFile = new File(targetDir, selectedFile[0].getName());

					Files.copy(selectedFile[0].toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

					JOptionPane.showMessageDialog(panel,
							"CV '" + selectedFile[0].getName() + "' saved to 'cv/' folder successfully!");

					selectedFile[0] = null;
					fileStatusLabel.setText("No file selected.");
					fileStatusLabel.setForeground(Color.GRAY);
					uploadBtn.setEnabled(false);

				} catch (IOException ioException) {
					JOptionPane.showMessageDialog(panel, "Failed to save file: " + ioException.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		panel.add(title, gbc);
		gbc.gridy = 1;
		panel.add(selectFileBtn, gbc);
		gbc.gridy = 2;
		panel.add(fileStatusLabel, gbc);
		gbc.gridy = 3;
		panel.add(uploadBtn, gbc);

		return panel;
	}

	// 7. Contact Us Panel
	private JPanel createContactUsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.anchor = GridBagConstraints.WEST;

		JLabel title = new JLabel("Contact Us");
		title.setFont(new Font("SansSerif", Font.BOLD, 20));
		panel.add(title, gbc);

		gbc.gridy++;
		JLabel adminHeader = new JLabel("System Admin: Raiyan Choudhury");
		adminHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
		panel.add(adminHeader, gbc);

		gbc.gridy++;
		panel.add(Box.createVerticalStrut(5), gbc);

		gbc.gridy++;
		panel.add(createLinkRow("Email:", "raiyanc01@gmail.com", "mailto:raiyanc01@gmail.com"), gbc);

		gbc.gridy++;
		panel.add(createLinkRow("GitHub:", "github.com/raiyanchoudhury", "https://github.com/raiyanchoudhury"), gbc);

		gbc.gridy++;
		panel.add(createLinkRow("LinkedIn:", "linkedin.com/in/raiyanchoudhury",
				"https://linkedin.com/in/raiyanchoudhury"), gbc);

		gbc.gridy++;
		panel.add(createLinkRow("Facebook:", "facebook.com/raiyanchoudhury", "https://facebook.com/raiyanchoudhury17"),
				gbc);

		return panel;
	}

	private JPanel createLinkRow(String labelText, String displayText, String targetUrl) {
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		JLabel label = new JLabel(labelText);
		label.setFont(new Font("SansSerif", Font.BOLD, 13));

		JLabel linkLabel = new JLabel("<html><a href=''>" + displayText + "</a></html>");
		linkLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		linkLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(targetUrl));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		row.add(label);
		row.add(linkLabel);
		return row;
	}
}