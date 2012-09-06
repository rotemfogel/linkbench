package com.facebook.LinkBench;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

import com.facebook.LinkBench.RealDistribution.DistributionType;
import com.facebook.LinkBench.distributions.LinkDistributions.ProbLinkDistribution;

/*
 * This class measures how similar data generated by class RealDistribution 
 * and the data file is.
 */
public class TestRealDistribution extends TestCase {

  Properties props;
  
  @Override
  public void setUp() throws Exception {
    props = new Properties();
    String distFile = new File("config/Distribution.dat").getAbsolutePath();
    props.setProperty(Config.DISTRIBUTION_DATA_FILE, distFile);
    RealDistribution.loadOneShot(props);
  }
  
  /** regression test for binary search bug */
  public void testBinSearchRegression1() {
    ArrayList<RealDistribution.Point> l = 
          new ArrayList<RealDistribution.Point>();
    l.add(new RealDistribution.Point(0, 0.0));
    l.add(new RealDistribution.Point(1, 0.5));
    l.add(new RealDistribution.Point(2, 1.0));
    assertEquals(0, RealDistribution.binarySearch(l, 0.00));
    assertEquals(1, RealDistribution.binarySearch(l, 0.001));
    assertEquals(1, RealDistribution.binarySearch(l, 0.4));
    assertEquals(1, RealDistribution.binarySearch(l, 0.5));
    assertEquals(2, RealDistribution.binarySearch(l, 0.50001));
    assertEquals(2, RealDistribution.binarySearch(l, 1.0));
    assertEquals(3, RealDistribution.binarySearch(l, 1.33));
  }

  /** regression test for binary search bug */
  public void testBinSearchRegression2() {
    double l[] =  new double[3];
    l[0] = 0.0;
    l[1] = 0.5;
    l[2] = 1.0;
    assertEquals(0, RealDistribution.binarySearch(l, 0.00));
    assertEquals(1, RealDistribution.binarySearch(l, 0.001));
    assertEquals(1, RealDistribution.binarySearch(l, 0.4));
    assertEquals(1, RealDistribution.binarySearch(l, 0.5));
    assertEquals(2, RealDistribution.binarySearch(l, 0.50001));
    assertEquals(2, RealDistribution.binarySearch(l, 1.0));
    assertEquals(3, RealDistribution.binarySearch(l, 1.33));
  }

  @Test
  public void testGetNLinks() throws Exception {
    //TODO: would be good to have some real measure for whether these
    //      error values are in the "proper" range
    System.out.println("testGetNLinks\n===========");
    
    double err;
    err = testGetNlinks(props, 1000000, 2000001);
    System.out.println("testGetNlinks(1000000, 2000001) err=" + err);
    assertTrue(err < 0.0001);
    
    err = testGetNlinks(props, 1234567, 7654321);
    System.out.println("testGetNlinks(1234567, 7654321) err=" + err);
    assertTrue(err < 0.0001);
    
    err = testGetNlinks(props, 97, 10000097);
    System.out.println("testGetNlinks(97, 10000097) err=" + err);
    assertTrue(err < 0.0001);
    System.out.println();
  }
  
  @Test
  public void testGetNextId1() throws Exception {
    //TODO: would be good to have some real measure for whether these
    //      error values are in the "proper" range
    System.out.println("testGetNextId1\n===========");
    
    long randSeed = System.currentTimeMillis();
    System.out.println("random seed: " + randSeed);
    Random rng = new Random(randSeed);
    
    double err;
    err = testGetNextId1(props, rng, 1000000, 2000001, DistributionType.LINK_READS);
    System.out.println("testGetNextId1(1000000, 2000001, nreads) err=" + err);
    err = testGetNextId1(props, rng, 1000000, 2000001, DistributionType.LINK_WRITES);
    System.out.println("testGetNextId1(1000000, 2000001, nwrites) err=" + err);
    err = testGetNextId1(props, rng, 1000000, 2000001, DistributionType.NODE_READS);
    System.out.println("testGetNextId1(1000000, 2000001, node_nreads) err=" + err);
    err = testGetNextId1(props, rng, 1000000, 2000001, DistributionType.NODE_WRITES);
    System.out.println("testGetNextId1(1000000, 2000001, node_nwrites) err=" + err);

    err = testGetNextId1(props, rng, 1234567, 7654321, DistributionType.LINK_READS);
    System.out.println("testGetNextId1(1234567, 7654321, nreads) err=" + err);
    err = testGetNextId1(props, rng, 1234567, 7654321, DistributionType.LINK_WRITES);
    System.out.println("testGetNextId1(1234567, 7654321, nwrites) err=" + err);
  
    err = testGetNextId1(props, rng, 97, 10000097, DistributionType.LINK_READS);
    
    System.out.println("testGetNextId1(97, 10000097, nreads) err=" + err);
    err = testGetNextId1(props, rng, 97, 10000097, DistributionType.LINK_WRITES);
    System.out.println("testGetNextId1(97, 10000097, nwrites) err=" + err);
    System.out.println();
  }
  
  //return the distribution for a sequence of numbers
  private static NavigableMap<Integer, Double> getDistribution(int[] seq,
                      int start, int end) {
    //create a map from values to number of times they appears in the sequence
    SortedMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
    for (int i = start; i < end; ++i) {
      Integer p = map.get(seq[i]);
      if (p==null) map.put(seq[i], 1);
      else map.put(seq[i], p + 1);
    }

    //calculate the cumulative distribution of map
    TreeMap<Integer, Double> cdf = new TreeMap<Integer, Double>();
    double sum = 0;
    for (Object key : map.keySet()) {
      sum += map.get((Integer)key) / (double) (end - start);
      cdf.put((Integer)key, sum);
    }
    return cdf;
  }

  //return the rms error between two distributions
  private static double getComparisonError(NavigableMap<Integer, Double> act,
      NavigableMap<Integer, Double> exp, boolean printBigErrors) {
    int min = Math.min(act.firstKey(), exp.firstKey());
    int max = Math.min(act.lastKey(), exp.lastKey());
    int samplePoints = Math.min(10000, max - min + 1);
    double cumulative = 0;
    
    // Sample linearly interpolated distribution at many points 
    for (int i = 0; i < samplePoints; i++) {
      int k = min + (int)Math.round(((max - min) *
                          ((double)i)/((double)samplePoints)));
      double pa = interpolatedValue(act, k);
      double pe = interpolatedValue(exp, k);

      cumulative += (pa - pe) * (pa - pe);
      // Print errors > 0.1%
      if (printBigErrors && Math.abs(pa - pe) > 0.001) {
        System.err.println(String.format("Large divergence %f "
                + "cdf_exp(%d) = %f, cdf_act(%d) = %f", 
                Math.abs(pa - pe), k, pe, k, pa));
      }
    }
    return cumulative / samplePoints;
  }

  private static double interpolatedValue(NavigableMap<Integer, Double> a, int k) {
    Entry<Integer, Double> floor = a.floorEntry(k);
    Entry<Integer, Double> ceil = a.ceilingEntry(k);
    if (ceil.getKey() == floor.getKey()) {
      return ceil.getValue();
    }
    double mix = (k - floor.getKey()) / 
          (double)(ceil.getKey() - floor.getKey());
    return mix * ceil.getValue() + (1 - mix) * floor.getValue();
  }

  //test RealDistribution.getNextId1
  //type is either "nlinks" or "nwrites"
  //maxid1 is exclusive
  private static double testGetNextId1(Properties props, Random rng,
                                       int startid1, int maxid1,
                                       DistributionType type) throws Exception {
    int[] cnt = new int[maxid1];
    
    double nqueries = (maxid1 - startid1)*RealDistribution.getArea(type)
      /100.0;
    
    RealDistribution dist = new RealDistribution();
    dist.init(props, startid1, maxid1, type);
    for (int i = 0; i < nqueries; ++i) {
      long x = dist.choose(rng);
      if (x < startid1 || x >= maxid1) {
        throw new Exception("Invalid value of id1: " + x);
      }
      cnt[(int)x]++;
    }
    
    NavigableMap<Integer, Double> generated_data =
                      getDistribution(cnt, startid1, maxid1);
    NavigableMap<Integer, Double> real_data = RealDistribution.getCDF(type);
    return getComparisonError(generated_data, real_data, true);
  }

  //test getNLinks
  private static double testGetNlinks(Properties props,
                                    int startid1, int maxid1) 
  throws Exception {
    RealDistribution rDist = new RealDistribution();
    rDist.init(props, startid1, maxid1, DistributionType.LINKS);
    ProbLinkDistribution dist = new ProbLinkDistribution(rDist);
    int[] nlinks = new int[maxid1];
    for (int i = startid1; i < maxid1; ++i) {
      long x = dist.getNlinks(i);
      if (x < 0) {
        fail("x is negative: " + x + " for i=" + i);
      }
      nlinks[i] = (int)x;
    }

    NavigableMap<Integer, Double> generated_data = getDistribution(nlinks,
                                                        startid1, maxid1);
    NavigableMap<Integer, Double> real_data = RealDistribution.getCDF(DistributionType.LINKS);
    return getComparisonError(generated_data, real_data, true);
  }
}

