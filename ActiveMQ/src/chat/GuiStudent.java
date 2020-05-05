package chat;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.awt.Color;
// import java.io.BufferedWriter;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class GuiStudent extends JFrame implements ActionListener, KeyListener, MessageListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 763321497521696765L;

	private JPanel contentPane;

	// 文本域
	private JTextPane jta;
	// 滚动条
	private JScrollPane jsp;
	// 面板
	private JPanel jp;
	// 文本框
	private JTextField jtf;
	// 按钮
	private JButton jb;
	private SimpleAttributeSet consumerStyle;
	private SimpleAttributeSet producerStyle;
	private SimpleAttributeSet fontStyle;

	// ConnectionFactory: 连接工厂，JMS 用它创建连接
	ConnectionFactory connectionFactory;
	// Connection: JMS 客户端到JMS Provider 的连接
	Connection connection = null;
	// Session: 一个发送或接收消息的线程
	Session session1, session2;
	// Destination: 消息的目的地;消息发送给谁.
	Destination destination1, destination2;
	// MessageProducer: 消息发送者
	MessageProducer producer;
	// MessageConsumer: 消息接收者
	MessageConsumer consumer;
	// label:版权信息
	Label label;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiStudent frame = new GuiStudent();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public GuiStudent() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 760, 720);
		setTitle("Student");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// 初始化组件
		// 文本域
		jta = new JTextPane();
		jta.setEditable(false);// 设置文本域不可以编辑
		// 注意：需要将文本域添加到滚动条上，实现滚动效果。
		jsp = new JScrollPane(jta);
		// 面板
		jp = new JPanel();

		// 文本框，参数是大小
		jtf = new JTextField(10);
		// 按钮
		jb = new JButton("Send");
		// 标签
		label = new Label("信管高云帆小组");

		// 需要将文本框与按钮添加到面板中
		jp.add(jtf);
		jp.add(jb);
		jp.add(label);

		// 窗口   个参数是添加滚动条和面板，后面是布局位置
		this.add(jsp, BorderLayout.CENTER);
		this.add(jp, BorderLayout.SOUTH);

		// 按钮帮定监听事件
		jb.addActionListener(this);
		jtf.addKeyListener(this);
		styleInit();

		queue();
	}

	void queue() {
		// TextMessage message;
		// 构造ConnectionFactory实例对象，此处采用ActiveMq的实现jar
		connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");

		try {
			// 构造从工厂得到连接对象
			connection = connectionFactory.createConnection();
			// 启动
			connection.start();
			// 获取操作连接
			session1 = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
			session2 = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
			// 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
			destination1 = session1.createQueue("Student");
			destination2 = session2.createQueue("Teacher");
			// 得到消息生成者【发送者】
			producer = session1.createProducer(destination1);
			// 设置不持久化，此处学习，实际根据项目决定
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			// 构造消息，此处写死，项目就是参数，或者方法获取
			consumer = session2.createConsumer(destination2);
			// sendMessage(session, producer);
			consumer.setMessageListener(this);
			session1.commit();
			// receive();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void send() throws JMSException {
		String text = jtf.getText();
		String show_text = text + System.lineSeparator();
		String user = "Student: " + getTime() + System.lineSeparator();
		insert(show_text, user, producerStyle);

		TextMessage message = session1.createTextMessage(text);
		// 发送消息到目的地方
		producer.send(message);
		session1.commit();
		jtf.setText("");
	}

	private void insert(String str, String user, SimpleAttributeSet attrSet) {
		Document document = jta.getDocument();
		try {
			document.insertString(document.getLength(), user, attrSet);
			document.insertString(document.getLength(), str, fontStyle);
		} catch (BadLocationException e) {
			System.out.println("BadLocationException:" + e);
		}
	}

	private void styleInit() {
		fontStyle = new SimpleAttributeSet();
		StyleConstants.setFontFamily(fontStyle, "楷体");
		StyleConstants.setFontSize(fontStyle, 18);
		StyleConstants.setForeground(fontStyle, Color.black);
		producerStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(producerStyle, Color.green);
		StyleConstants.setFontSize(producerStyle, 14);
		consumerStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(consumerStyle, Color.blue);
		StyleConstants.setFontSize(consumerStyle, 14);
	}

	private String getTime() {
		Date date = new Date();
		DateFormat formatdate = DateFormat.getDateTimeInstance();
		return formatdate.format(date);
	}

	// 监听点击事件
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			send();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				send();
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(Message arg0) {
		try {
			String text = ((TextMessage) arg0).getText() + System.lineSeparator();
			String user = "Teacher: " + getTime() + System.lineSeparator();
			insert(text, user, consumerStyle);
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

}
