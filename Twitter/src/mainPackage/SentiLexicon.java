package mainPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SentiLexicon
{

	public HashMap<String, Double> dictionary = new HashMap<String, Double>();

	public SentiLexicon() throws IOException
	{

		/*
         Η βασική αναπαράσταση του λεξικού, απο String σε λίστα απο double.
		 */
		BufferedReader csvPos = null;
		BufferedReader csvNeg = null;
		BufferedReader csvPos2 = null;
		BufferedReader csvNeg2 = null;
		BufferedReader csvNeg3 = null;
		String positive = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/LoughranMcDonald_Positive.txt";
		String positive2 = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/positive-words.txt";
		String negative = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/LoughranMcDonald_Negative.txt";
		String negative3 = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/SentiWordNet_3.0.0_20130122.txt";
		String negative2 = "C:/Users/user/git/Twitter/Twitter/opinion-lexicon-English/negative-words.txt";
		HashMap<String, HashMap<Integer, Double>> tempDictionary = new HashMap<String, HashMap<Integer, Double>>();
		try
		{
			csvPos = new BufferedReader(new FileReader(positive));
			csvNeg = new BufferedReader(new FileReader(negative));
			csvPos2 = new BufferedReader(new FileReader(positive2));
			csvNeg2 = new BufferedReader(new FileReader(negative2));
			csvNeg3 = new BufferedReader(new FileReader(negative3));
			int lineNumber = 0;

			String line;
			/*while ((line = csvPos.readLine()) != null)
			{
				lineNumber++;

				/*
                 Αν είναι σχόλιο, μην το χρησιμοποιήσεις.
				 *
				if (!line.trim().startsWith("#"))
				{
					dictionary.put(line.toLowerCase(), 1.0);
				}
			}
			while ((line = csvNeg.readLine()) != null)
			{
				lineNumber++;

				if (!line.trim().startsWith("#"))
				{
					dictionary.put(line.toLowerCase(), -1.0);
				}
			}*/

			while ((line = csvPos2.readLine()) != null)
			{
				lineNumber++;

				if (!line.trim().startsWith("#"))
				{
					dictionary.put(line.toLowerCase(), 1.0);
				}
			}
			while ((line = csvNeg2.readLine()) != null)
			{
				lineNumber++;

				if (!line.trim().startsWith("#"))
				{
					dictionary.put(line.toLowerCase(), -1.0);
				}
			}

			/*while ((line = csvNeg3.readLine()) != null)
			{ 
				lineNumber++; 

				// If it's a comment, skip this line. 
				if (!line.trim().startsWith("#"))
				{ 
					// We use tab separation 
					String[] data = line.split("\t"); 
					String wordTypeMarker = data[0]; 

					// Example line: 
					// POS ID PosS NegS SynsetTerm#sensenumber Desc 
					// a 00009618 0.5 0.25 spartan#4 austere#3 ascetical#2 
					// ascetic#2 practicing great self-denial;...etc 

					// Is it a valid line? Otherwise, through exception. 
					if (data.length != 6)
					{ 
						throw new IllegalArgumentException( 
								"Incorrect tabulation format in file, line: "
										+ lineNumber); 
					} 

					// Calculate synset score as score = PosS - NegS 
					Double synsetScore = Double.parseDouble(data[2]) 
							- Double.parseDouble(data[3]); 

					// Get all Synset terms 
					String[] synTermsSplit = data[4].split(" "); 

					// Go through all terms of current synset. 
					for (String synTermSplit : synTermsSplit)
					{ 
						// Get synterm and synterm rank 
						String[] synTermAndRank = synTermSplit.split("#"); 
						String synTerm = synTermAndRank[0] + "#"
								+ wordTypeMarker; 

						int synTermRank = Integer.parseInt(synTermAndRank[1]); 
						// What we get here is a map of the type: 
						// term -> {score of synset#1, score of synset#2...} 

						// Add map to term if it doesn't have one 
						if (!tempDictionary.containsKey(synTerm)) { 
							tempDictionary.put(synTerm, 
									new HashMap<Integer, Double>()); 
						} 

						// Add synset link to synterm 
						tempDictionary.get(synTerm).put(synTermRank, 
								synsetScore); 
					} 
				} 
			}

			for (Map.Entry<String, HashMap<Integer, Double>> entry : tempDictionary 
					.entrySet())
			{ 
				String word = entry.getKey(); 
				Map<Integer, Double> synSetScoreMap = entry.getValue(); 

				// Calculate weighted average. Weigh the synsets according to 
				// their rank. 
				// Score= 1/2*first + 1/3*second + 1/4*third ..... etc. 
				// Sum = 1/1 + 1/2 + 1/3 ... 
				double score = 0.0; 
				double sum = 0.0; 
				for (Map.Entry<Integer, Double> setScore : synSetScoreMap 
						.entrySet())
				{ 
					score += setScore.getValue() / (double) setScore.getKey(); 
					sum += 1.0 / (double) setScore.getKey(); 
				} 
				score /= sum; 

				dictionary.put(word, score); 
			}*/


		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (csvPos != null)
			{
				csvPos.close();
			}
			if (csvNeg != null)
			{
				csvNeg.close();
			}
			if (csvPos2 != null)
			{
				csvPos2.close();
			}
			if (csvNeg2 != null)
			{
				csvNeg2.close();
			}
			if (csvNeg3 != null)
			{
				csvNeg3.close();
			}
		}

	}

	public double getScore(String sentence)
	{
		double sum = 0;
		String[] words = sentence.split(" ");
		for (int i = 0; i < words.length; i++)
		{
			if (dictionary.get(words[i].toLowerCase()) != null)
			{
				sum += dictionary.get(words[i].toLowerCase());
			}
		}
		return sum;
	}

	public double extract(String word, String pos)
	{ 
		if (dictionary.containsKey(word + "#" + pos)){ 
			return dictionary.get(word + "#" + pos);}return 0; 
	} 

	public double extractScore(String sentence)
	{   	
		double sum = 0; 
		double i=0.00000001;
		if (sentence == null)
			return 0;
		String[] words = sentence.split(" ");

		for (int j = 0; j < words.length; j++)
		{
			if (dictionary.get(words[j]) != null)
			{
				if (extract(words[j], "n")!=0){i++;} 
				if (extract(words[j], "a")!=0){i++;} 
				if (extract(words[j], "r")!=0){i++;} 
				if (extract(words[j], "v")!=0){i++;}
				sum += (extract(words[j], "n")+extract(words[j], "a")+extract(words[j], "r")+extract(words[j], "v"))/i;
			}
		}


		return sum; 
	}

}
