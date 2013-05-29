/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hermes.stats;

import hermes.HermesConfig;
import hermes.runtime.HermesRuntime;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rmartins
 */
public class Stats {

    private String m_run;
    private String m_prefix;
    private String m_attack;

    public Stats() {
    }
    static final int RUNS = 1000;
    static final int HALFRUNS = RUNS / 2;
    long m_start = 0;
    long m_end = 0;
    List<StatToken> m_l = new ArrayList<>();

    public void start() {
        m_start = System.nanoTime();
    }

    public void end() {
        m_end = System.nanoTime();
    }

    public void collect(int i, long duration) {
        m_l.add(new StatToken(i, duration));
    }

    public void collect(StatToken st) {
        m_l.add(st);
    }

    public long sumAboveHalf() {
        long sum = 0;
        for (StatToken s : m_l) {
            if (s.m_i >= HALFRUNS) {
                sum += s.m_duration;
            }
        }
        return sum;
    }

    public long bellowAboveHalf() {
        long sum = 0;
        for (StatToken s : m_l) {
            if (s.m_i < HALFRUNS) {
                sum += s.m_duration;
            }
        }
        return sum;
    }

    public boolean isFinished() {
        return (m_l.size() >= RUNS - 1);
    }

    public void dump() {
        FileWriter fw = null;
        try {
            float duration = 0;
            if (m_l.size() != 1000) {
                duration = 0;
            } else {
                duration = ((float) (m_end - m_start) / (float) 1000000);
            }
            StringBuffer bf = new StringBuffer();
            BigDecimal bd = new BigDecimal(duration).setScale(2, RoundingMode.HALF_UP);
            bf.append(bd.floatValue() + "," + "\n\n");
            for (StatToken s : m_l) {
                s.dump(bf);
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            Date date = new Date();
            dateFormat.format(date);            
            
            String logName = HermesConfig.getWorkingDir() + "/results"+
                    "_"+m_prefix+
                    "_"+m_attack+
                    "_"+m_run+                    
                    "_" + dateFormat.format(date) + ".log";
            //System.out.println(bf.toString());
            File file = new File(logName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(bf.toString());
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Stats.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(Stats.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setPrefix(String prefix,String run,String attack) {
        m_run = run;
        m_prefix = prefix;
        m_attack = attack;
    }
}
