package com.qinyun.bbs.lucene.dto.jieba;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.springframework.util.ResourceUtils;

import com.huaban.analysis.jieba.JiebaSegmenter;

public class JiebaAnalyzer extends Analyzer{

	private static final String DEFAULT_STOPWORD_FILE = "stopwords.txt";
    private final CharArraySet stopWords;

    private JiebaSegmenter.SegMode segMode;
	
	private boolean useSmart;
	
	public boolean useSmart() {
		return useSmart;
	}

	public void setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
	}
	
	public JiebaAnalyzer(){
		this(false);
	}
	
	public JiebaAnalyzer(boolean useSmart){
		super();
		this.useSmart = useSmart;
	        this.segMode = useSmart?JiebaSegmenter.SegMode.INDEX:JiebaSegmenter.SegMode.SEARCH;
	        this.stopWords = new CharArraySet(128, true);
	        File file;
			try {
				file = ResourceUtils.getFile("classpath:"+DEFAULT_STOPWORD_FILE);
				init(file.getParent());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	        
	    }

	    /**
	     * use for add user dictionary and stop words,
	     * user dictionary need with .dict suffix, stop words with file name: stopwords.txt
	     * @param userDictPath
	     */
	    public void init(String userDictPath){
	        if ( ! StringUtils.isEmpty(userDictPath)){
	            File file  = new File(userDictPath);
	            if (file.exists()){
	                //load stop words from userDictPath with name stopwords.txt, one word per line.
	                loadStopWords(Paths.get(userDictPath, DEFAULT_STOPWORD_FILE) , Charset.forName("UTF-8"));
	            }
	        }
	    }

	    /**
	     * load stop words from path
	     * @param userDict stop word path, one word per line
	     * @param charset
	     */
	    private void loadStopWords(Path userDict, Charset charset) {
	        try {
	            BufferedReader br = Files.newBufferedReader(userDict, charset);
	            int count = 0;
	            while (br.ready()) {
	                String line = br.readLine();
	                if (! StringUtils.isEmpty(line)){
	                    stopWords.add(line);
	                    ++count;
	                }
	            }
	            System.out.println(String.format(Locale.getDefault(), "%s: load stop words total:%d!", userDict.toString(), count));
	            br.close();
	        }
	        catch (IOException e) {
	            System.err.println(String.format(Locale.getDefault(), "%s: load stop words failure!", userDict.toString()));
	        }
	    }

	    @Override
	    protected TokenStreamComponents createComponents(String s) {
	        final Tokenizer tokenizer = new JiebaTokenizer(segMode);
	        TokenStream result = tokenizer;

	        if (!stopWords.isEmpty()) {
	            result = new StopFilter(result, stopWords);
	        }

	        return new TokenStreamComponents(tokenizer, result);
	    }
}
