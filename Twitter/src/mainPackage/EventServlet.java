﻿package mainPackage;

import com.aliasi.classify.LMClassifier;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Servlet για την αρχικοποίηση στοιχείων που έχω 
 * αποθηκεύσει σε προηγούμενη εκτέλεση στο δίσκο 
 * και επεξεργασία αιτημάτων απο το jsp.
 */
@WebServlet(urlPatterns = {"/TestServlet"})
public class EventServlet extends HttpServlet
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	//private HashMap<String, Double> idf;
	//private HashMap<Long, HashMap<String, Integer>> postingMap;
	@SuppressWarnings("rawtypes")
	private LMClassifier classifier;
	private HelperUtil help;
	//private PreProcessing pre;
	//private String message;

	@SuppressWarnings("rawtypes")
	@Override
	public void init() throws ServletException
	{
		//pre = new PreProcessing();
		help = new HelperUtil();
		try
		{
			//παράδειγμα χρήσης της κλάσης PreProcessing. 
			//εδω μονο διαβάζω απο το δίσκο.
			//PreProcessing preprocessor = new PreProcessing();
			//idf = preprocessor.getIdf();
			//postingMap = preprocessor.getPostings();
			//classifier = preprocessor.getClassifier();
			//idf = (HashMap<String, Double>) SerializationUtil.deserialize("C:/Users/user/git/Twitter/Twitter/idf.dat");
			//System.out.println("Loaded idf");
			//postingMap = (HashMap<Long, HashMap<String, Integer>>) SerializationUtil.deserialize("C:/Users/user/git/Twitter/Twitter/postings.dat");
			//System.out.println("Loaded postings");
			classifier = (LMClassifier) SerializationUtil.deserialize("C:/Users/user/git/Twitter/Twitter/classifier.dat");
			System.out.println("Loaded classifier");
		}
		catch (Exception e)
		{
			System.out.println("Den fortothike! " + e.getLocalizedMessage());
		}

	}
	/*
     H μέθοδος processRequest αναλαμβάνει να επεξεργαστεί δεδομένα με βάση το είδος της HTTP
     μεθοδου που κλήθηκε (συγκεκριμένα POST).  
	 */

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, UnknownHostException, ParseException, Exception
	{
		response.setContentType("text/html;charset=UTF-8");

		try (PrintWriter out = response.getWriter())
		{
			Date startDate = new SimpleDateFormat("yyyy-MM-dd",
					Locale.ENGLISH).parse(request.getParameter("startDate"));

			Date endDate = new SimpleDateFormat("yyyy-MM-dd",
					Locale.ENGLISH).parse(request.getParameter("endDate"));
			//έλεγχος περίπτωσης οπου ο χρήστης έχει δωσει λάθος χρονικο διάστημα
			//redirect σε αντίστοιχη σελίδα λάθους
			if (startDate.getTime() - endDate.getTime() >= 0)
			{
				String redirectURL = "http://localhost:8080/Twitter/errorPage.html";
				response.sendRedirect(redirectURL);
				return;
			}

            /* 'Eλεγχος της παραμέτρου του τυπου της σελίδας που εκανε το 
             request.
             */
            if (((String) request.getParameter("type")).equals("event"))
            {
                //1ος τροπος

                /*
                 Κληση μεθοδου ευρεσης των event με τις αντίστοιχες παραμέτρους
                 τις οποίες παίρνουμε απο το request.
                 */
               /* String[] array = help.stringOfPeaks(startDate, endDate,
                        Integer.parseInt(request.getParameter("topKWords")),
                        Integer.parseInt(request.getParameter("topKUrls")),
                        Integer.parseInt(request.getParameter("topKTweets")),
                        Integer.parseInt(request.getParameter("Duration")), idf, postingMap);
                //ανάθεση του πίνακα των αποτελεσμάτων στην μεταβλητή array
                request.setAttribute("array", array);
                //Αποστολή αποτελεσματων και εμφάνισή τους.
                request.getRequestDispatcher("/eventResults.jsp").forward(request, response);
                array = null;*/
            }
            else
            {
                  /*
                 Κληση μεθοδου ευρεσης των στοιχείων των sentiment features με τις 
                 αντίστοιχες παραμέτρους τις οποίες παίρνουμε απο το request.
                 */
                String[] array = help.sentiStream(startDate, endDate,
                        classifier);
                //ανάθεση του πίνακα των αποτελεσμάτων στην μεταβλητή array
                request.setAttribute("array", array);
                //Αποστολή αποτελεσματων και εμφάνισή τους.
                request.getRequestDispatcher("/sentiResults.jsp").forward(request, response);

            }
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, UnknownHostException
    {
        try
        {
            processRequest(request, response);
        }
        catch (ParseException ex)
        {
            Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception ex)
        {
            Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, UnknownHostException
    {
    	try
    	{
    		processRequest(request, response);
    	}
    	catch (ParseException ex)
    	{
    		Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
    	}
    	catch (Exception ex)
    	{
    		Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
    	}
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

}
