package mainPackage;

import com.aliasi.classify.LMClassifier;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import twitter4j.TwitterException;

/**
 *
 * To servlet που χρησιμοποιουμε για την αρχικοποίηση στοιχείων που έχουμε 
 * αποθηκευσει σε προηγουμενη εκτέλεση στο δίσκο και επεξεργασία αιτημάτων του
 * χρήστη απο το jsp
 */
@WebServlet(urlPatterns = {"/TestServlet"})
public class EventServlet extends HttpServlet {

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
    private HashMap<String, Double> idf;
    private HashMap<Long, HashMap<String, Integer>> postingMap;
    private LMClassifier classifier;
    private HelperUtil help;
    private PreProcessing pre;
    private String message;

    @Override
    public void init() throws ServletException {
    	pre = new PreProcessing();
        help = new HelperUtil();
        try {
            //παράδειγμα χρήσης της κλάσης preProcessing εδω μονο διαβάζουμε 
            //απο τον δίσκο
            //PreProcessing preprocessor = new PreProcessing();
            //idf = preprocessor.getIdf();
            //postingMap = preprocessor.getPostings();
            //classifier = preprocessor.getClassifier();
            idf = (HashMap<String, Double>) SerializationUtil.deserialize(getServletContext().getRealPath("/idf.dat"));
            System.out.println("Loaded idf");
            postingMap = (HashMap<Long, HashMap<String, Integer>>) SerializationUtil.deserialize(getServletContext().getRealPath("/postings.dat"));
            System.out.println("Loaded postings");
            classifier = (LMClassifier) SerializationUtil.deserialize(getServletContext().getRealPath("/classifier.dat"));
            System.out.println("Loaded class");
        } catch (Exception e) {
            System.out.println("Mpeos " + e.getLocalizedMessage());
        }

    }
    /*
     H μέθοδος processRequest αναλαμβάνει να επεξεργαστεί δεδομένα με βάση το είδος της HTTP
     μεθοδου που κλήθηκε (συγκεκριμένα POST).  
     */

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, UnknownHostException, TwitterException, ParseException, Exception {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            Date startDate = new SimpleDateFormat("yyyy-MM-dd kk:mm",
                    Locale.ENGLISH).parse(request.getParameter("startDate")
                            + " " + request.getParameter("startTime"));

            Date endDate = new SimpleDateFormat("yyyy-MM-dd kk:mm",
                    Locale.ENGLISH).parse(request.getParameter("endDate")
                            + " " + request.getParameter("endTime"));
                //έλεγχος περίπτωσης οπου ο χρήστης έχει δωσει λάθος χρονικο διάστημα
            //redirect σε αντίστοιχη σελίδα λάθους
            if (startDate.getTime() - endDate.getTime() >= 0) {
                String redirectURL = "http://localhost:8080/Twitter/errorPage.html";
                response.sendRedirect(redirectURL);
                return;
            }

            /* 'Eλεγχος της παραμέτρου του τυπου της σελίδας που εκανε το 
             request.
             */
            if (((String) request.getParameter("type")).equals("event")) {
                //1ος τροπος

                /*
                 Κληση μεθοδου ευρεσης των event με τις αντίστοιχες παραμέτρους
                 τις οποίες παίρνουμε απο το request.
                 */
                String[] array = help.stringOfPeaks(startDate, endDate,
                        Integer.parseInt(request.getParameter("topKWords")),
                        Integer.parseInt(request.getParameter("topKUrls")),
                        Integer.parseInt(request.getParameter("topKTweets")),
                        Integer.parseInt(request.getParameter("Duration")), idf, postingMap);
                //ανάθεση του πίνακα των αποτελεσμάτων στην μεταβλητή array
                request.setAttribute("array", array);
                //Αποστολή αποτελεσματων και εμφάνισή τους.
                request.getRequestDispatcher("/eventResults.jsp").forward(request, response);
                array = null;
            } else {
                //2ος τροπος
                  /*
                 Κληση μεθοδου ευρεσης των στοιχείων των sentiment features με τις 
                 αντίστοιχες παραμέτρους τις οποίες παίρνουμε απο το request.
                 */
                String[] array = help.sentiStream(startDate, endDate,
                        Integer.parseInt(request.getParameter("Duration")),
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
            throws ServletException, IOException, UnknownHostException {
        try {
            processRequest(request, response);
        } catch (TwitterException ex) {
            Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
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
            throws ServletException, IOException, UnknownHostException {
        try {
            processRequest(request, response);
        } catch (TwitterException ex) {
            Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EventServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
