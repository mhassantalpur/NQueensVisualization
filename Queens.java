import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

public class Queens extends JFrame implements Runnable
{
	public ChessSquare [][] squares;
	public boolean [] saferow;
	public boolean [] safeleftdiag;
	public boolean [] saferightdiag;
  private ShapePanel drawPanel; // panel for the board to be drawn
	private JLabel info;		// informative label
	private JButton runDemo;	// button to allow interaction
	private Thread runThread;	// thread to allow "motion"
	private int delay;			// delay between moves
	private PauseClass pauser;	// class to allow execution to pause in between
	private boolean paused;		// is execution paused
	private int sol, calls;

  public Queens(int delta)
  {
    super("Interactive 8 Queens Problem");
    delay = delta;
    drawPanel = new ShapePanel(450, 450);

    runDemo = new JButton("See Solutions");
    ButtonHandler bhandler = new ButtonHandler();
    runDemo.addActionListener(bhandler);

    info = new JLabel("The 8 Queens Problem", (int) CENTER_ALIGNMENT);
    Container c = getContentPane();
    c.add(drawPanel, BorderLayout.CENTER);
    c.add(info, BorderLayout.NORTH);
    c.add(runDemo, BorderLayout.SOUTH);

    squares = new ChessSquare[8][8];
    saferow = new boolean[8];
    safeleftdiag = new boolean[15];
    saferightdiag = new boolean[15];
    int size = 50;
    int offset = 25;
    for (int row = 0; row < 8; row++)
    {
      saferow[row] = true;  // all rows are safe
      for (int col = 0; col < 8; col++)
      {
        squares[row][col] = new ChessSquare(offset + col*size,offset + row*size,size,size);
      }
    }
    for (int i = 0; i < 15; i++)
    {
      safeleftdiag[i] = true;
      saferightdiag[i] = true;
    }
    sol = 0;
    calls = 0;
    runThread = null;
    setSize(475, 525);
    setVisible(true);
  }

  public boolean safe(int row, int col)
	{
		return (saferow[row] && safeleftdiag[row+col] &&
			saferightdiag[row-col+7]);
	}

  public void trycol(int col)
	{
		calls++;
		for (int row = 0; row < 8; row++)
		{
			if (safe(row, col))
			{
				saferow[row] = false;
				safeleftdiag[row+col] = false;
				saferightdiag[row-col+7] = false;
				(squares[row][col]).occupy();
				repaint();

				if (col == 7)
				{
					sol++;
					info.setText("Solution " + sol + " Found After " + calls + " Calls");
					runDemo.setText("Click to Continue");
					runDemo.setEnabled(true);
					pauser.pause();
				}
				else
				{
					try
					{
						Thread.sleep(delay);
					}
					catch (InterruptedException e)
					{
						System.out.println("Thread error B");
					}

					trycol(col+1);
				}

				saferow[row] = true;
				safeleftdiag[row+col] = true;
				saferightdiag[row-col+7] = true;
				(squares[row][col]).remove();
			}
		}
	}

  public void run()
  {
    paused = false;
		pauser = new PauseClass();
    trycol(0);
		repaint();
    info.setText("Program Completed: " + sol + " Solutions, "+ calls + " Calls, "+ (8*calls) + " iterations ");
  }

  public static void main(String [] args)
  {
    // Use the delay value entered by the user, or use 100 if no
		// value is entered.
		int delay;
		if (args != null && args.length >= 1)
    delay = Integer.parseInt(args[0]);
		else
      delay = 100;

      Queens win = new Queens(delay);

      win.addWindowListener(
        new WindowAdapter()
        {
          public void windowClosing(WindowEvent e)
          { System.exit(0); }
        }
      );
  }

  private class PauseClass
	{
		public synchronized void pause()
		{
			paused = true;
			try
			{
				wait();
			}
			catch (InterruptedException e)
			{
				System.out.println("Pause Problem");
			}
		}

		public synchronized void unpause()
		{
			paused = false;
			notify();
		}
	}

	private class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == runDemo)
			{
				if (!paused)
				{
					runDemo.setEnabled(false);
					info.setText("Searching for Solutions");
					runThread = new Thread(Queens.this);
					runThread.start();
				}
				else
				{
					runDemo.setEnabled(false);
					info.setText("Searching for Solutions");
					pauser.unpause();
				}
				repaint();
			}
		}
	}

	private class ChessSquare extends Rectangle2D.Double
	{
		private boolean occupied;

		public ChessSquare(double x1, double y1, double wid, double hei)
		{
			super(x1, y1, wid, hei);
			occupied = false;
		}

		public void draw(Graphics2D g2d)
		{
			g2d.draw(this);
			int x = (int) this.getX();
			int y = (int) this.getY();
			int sz = (int) this.getWidth();

			if (occupied)
			{
				g2d.setFont(new Font("Serif", Font.BOLD, 36));
				g2d.drawString("Q", x+10, y+sz-10);
			}
		}

		public void occupy()
		{
			occupied = true;
		}

		public void remove()
		{
			occupied = false;
		}

		public boolean isOccupied()
		{
			return occupied;
		}
	}

  private class ShapePanel extends JPanel
  {
    private int prefwid, prefht;
    public ShapePanel (int pwid, int pht)

    {
      prefwid = pwid;
      prefht = pht;
    }

    public Dimension getPreferredSize()
    {
      return new Dimension(prefwid, prefht);
    }

    public void paintComponent (Graphics g)
    {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      for (int i = 0; i < 8; i++)
      {
        for (int j = 0; j < 8; j++)
        {
          (squares[i][j]).draw(g2d);
        }
      }
    }
  }
}
