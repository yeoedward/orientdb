package com.orientechnologies.orient.core.index.sbtree.local;

import java.util.*;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.common.util.MersenneTwisterFast;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.OClusterPositionFactory;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.storage.impl.local.OStorageLocalAbstract;

/**
 * @author Andrey Lomakin
 * @since 12.08.13
 */
@Test
public class SBTreeTest {
  private static final int    KEYS_COUNT = 500000;

  private ODatabaseDocumentTx databaseDocumentTx;

  protected OSBTree<Integer>  sbTree;
  private String              buildDirectory;

  @BeforeClass
  public void beforeClass() {
    buildDirectory = System.getProperty("buildDirectory");
    if (buildDirectory == null)
      buildDirectory = ".";

    databaseDocumentTx = new ODatabaseDocumentTx("plocal:" + buildDirectory + "/localSBTreeTest");
    if (databaseDocumentTx.exists()) {
      databaseDocumentTx.open("admin", "admin");
      databaseDocumentTx.drop();
    }

    databaseDocumentTx.create();

    sbTree = new OSBTree<Integer>(".sbt", 1, false);
    sbTree.create("sbTree", OIntegerSerializer.INSTANCE, (OStorageLocalAbstract) databaseDocumentTx.getStorage());
  }

  @AfterMethod
  public void afterMethod() throws Exception {
    sbTree.clear();
  }

  @AfterClass
  public void afterClass() throws Exception {
    sbTree.clear();
    sbTree.delete();
    databaseDocumentTx.drop();
  }

  public void testKeyPut() throws Exception {
    for (int i = 0; i < KEYS_COUNT; i++) {
      sbTree.put(i, new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));
    }

    for (int i = 0; i < KEYS_COUNT; i++)
      Assert.assertEquals(sbTree.get(i), new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)), i
          + " key is absent");

    for (int i = KEYS_COUNT; i < 2 * KEYS_COUNT; i++)
      Assert.assertNull(sbTree.get(i));

  }

  public void testKeyPutRandomUniform() throws Exception {
    final Set<Integer> keys = new HashSet<Integer>();
    final MersenneTwisterFast random = new MersenneTwisterFast();

    while (keys.size() < KEYS_COUNT) {
      int key = random.nextInt(Integer.MAX_VALUE);
      sbTree.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
      keys.add(key);

      Assert.assertEquals(sbTree.get(key), new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
    }

    for (int key : keys)
      Assert.assertEquals(sbTree.get(key), new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
  }

  public void testKeyPutRandomGaussian() throws Exception {
    Set<Integer> keys = new HashSet<Integer>();
    long seed = 1376477211861L;

    System.out.println("testKeyPutRandomGaussian seed : " + seed);

    MersenneTwisterFast random = new MersenneTwisterFast(seed);

    while (keys.size() < KEYS_COUNT) {
      int key = (int) (random.nextGaussian() * Integer.MAX_VALUE / 2 + Integer.MAX_VALUE);
      if (key < 0)
        continue;

      sbTree.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
      keys.add(key);

      Assert.assertEquals(sbTree.get(key), new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
    }

    for (int key : keys)
      Assert.assertEquals(sbTree.get(key), new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
  }

  public void testKeyDeleteRandomUniform() throws Exception {
    HashSet<Integer> keys = new HashSet<Integer>();
    for (int i = 0; i < KEYS_COUNT; i++) {
      sbTree.put(i, new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));
      keys.add(i);
    }

    for (int key : keys) {
      if (key % 3 == 0)
        sbTree.remove(key);
    }

    for (int key : keys) {
      if (key % 3 == 0) {
        Assert.assertNull(sbTree.get(key));
      } else {
        Assert.assertEquals(sbTree.get(key), new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
      }
    }
  }

  public void testKeyDeleteRandomGaussian() throws Exception {
    HashSet<Integer> keys = new HashSet<Integer>();

    long seed = System.currentTimeMillis();

    System.out.println("testKeyDeleteRandomGaussian seed : " + seed);
    MersenneTwisterFast random = new MersenneTwisterFast(seed);

    while (keys.size() < KEYS_COUNT) {
      int key = (int) (random.nextGaussian() * Integer.MAX_VALUE / 2 + Integer.MAX_VALUE);
      if (key < 0)
        continue;

      sbTree.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
      keys.add(key);

      Assert.assertEquals(sbTree.get(key), new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
    }

    for (int key : keys) {
      if (key % 3 == 0)
        sbTree.remove(key);
    }

    for (int key : keys) {
      if (key % 3 == 0) {
        Assert.assertNull(sbTree.get(key));
      } else {
        Assert.assertEquals(sbTree.get(key), new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
      }
    }
  }

  public void testKeyDelete() throws Exception {
    for (int i = 0; i < KEYS_COUNT; i++) {
      sbTree.put(i, new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));
    }

    for (int i = 0; i < KEYS_COUNT; i++) {
      if (i % 3 == 0)
        Assert.assertEquals(sbTree.remove(i), new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));
    }

    for (int i = 0; i < KEYS_COUNT; i++) {
      if (i % 3 == 0)
        Assert.assertNull(sbTree.get(i));
      else
        Assert.assertEquals(sbTree.get(i), new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));
    }
  }

  public void testKeyAddDelete() throws Exception {
    for (int i = 0; i < KEYS_COUNT; i++) {
      sbTree.put(i, new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));

      Assert.assertEquals(sbTree.get(i), new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));
    }

    for (int i = 0; i < KEYS_COUNT; i++) {
      if (i % 3 == 0)
        Assert.assertEquals(sbTree.remove(i), new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));

      if (i % 2 == 0)
        sbTree.put(KEYS_COUNT + i,
            new ORecordId((KEYS_COUNT + i) % 32000, OClusterPositionFactory.INSTANCE.valueOf(KEYS_COUNT + i)));
    }

    for (int i = 0; i < KEYS_COUNT; i++) {
      if (i % 3 == 0)
        Assert.assertNull(sbTree.get(i));
      else
        Assert.assertEquals(sbTree.get(i), new ORecordId(i % 32000, OClusterPositionFactory.INSTANCE.valueOf(i)));

      if (i % 2 == 0)
        Assert.assertEquals(sbTree.get(KEYS_COUNT + i),
            new ORecordId((KEYS_COUNT + i) % 32000, OClusterPositionFactory.INSTANCE.valueOf(KEYS_COUNT + i)));
    }
  }

  public void testValuesMajor() {
    TreeMap<Integer, ORID> keyValues = new TreeMap<Integer, ORID>();
    MersenneTwisterFast random = new MersenneTwisterFast();

    while (keyValues.size() < KEYS_COUNT) {
      int key = random.nextInt(Integer.MAX_VALUE);

      sbTree.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
      keyValues.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
    }

    assertMajorValues(keyValues, random, true);
    assertMajorValues(keyValues, random, false);
  }

  public void testValuesMinor() {
    TreeMap<Integer, ORID> keyValues = new TreeMap<Integer, ORID>();
    MersenneTwisterFast random = new MersenneTwisterFast();

    while (keyValues.size() < KEYS_COUNT) {
      int key = random.nextInt(Integer.MAX_VALUE);

      sbTree.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
      keyValues.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
    }

    assertMinorValues(keyValues, random, true);
    assertMinorValues(keyValues, random, false);
  }

  public void testValuesBetween() {
    TreeMap<Integer, ORID> keyValues = new TreeMap<Integer, ORID>();
    MersenneTwisterFast random = new MersenneTwisterFast();

    while (keyValues.size() < KEYS_COUNT) {
      int key = random.nextInt(Integer.MAX_VALUE);

      sbTree.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
      keyValues.put(key, new ORecordId(key % 32000, OClusterPositionFactory.INSTANCE.valueOf(key)));
    }

    assertBetweenValues(keyValues, random, true, true);
    assertBetweenValues(keyValues, random, true, false);
    assertBetweenValues(keyValues, random, false, true);
    assertBetweenValues(keyValues, random, false, false);
  }

  private void assertMajorValues(TreeMap<Integer, ORID> keyValues, MersenneTwisterFast random, boolean keyInclusive) {
    for (int i = 0; i < 100; i++) {
      int upperBorder = keyValues.lastKey() + 5000;
      int fromKey;
      if (upperBorder > 0)
        fromKey = random.nextInt(upperBorder);
      else
        fromKey = random.nextInt(Integer.MAX_VALUE);

      if (random.nextBoolean()) {
        Integer includedKey = keyValues.ceilingKey(fromKey);
        if (includedKey != null)
          fromKey = includedKey;
        else
          fromKey = keyValues.floorKey(fromKey);
      }

      int maxValuesToFetch = 10000;
      Collection<ORID> orids = sbTree.getValuesMajor(fromKey, keyInclusive, maxValuesToFetch);

      Set<ORID> result = new HashSet<ORID>(orids);

      Iterator<ORID> valuesIterator = keyValues.tailMap(fromKey, keyInclusive).values().iterator();

      int fetchedValues = 0;
      while (valuesIterator.hasNext() && fetchedValues < maxValuesToFetch) {
        ORID value = valuesIterator.next();
        Assert.assertTrue(result.remove(value));

        fetchedValues++;
      }

      if (valuesIterator.hasNext())
        Assert.assertEquals(fetchedValues, maxValuesToFetch);

      Assert.assertEquals(result.size(), 0);
    }
  }

  private void assertMinorValues(TreeMap<Integer, ORID> keyValues, MersenneTwisterFast random, boolean keyInclusive) {
    for (int i = 0; i < 100; i++) {
      int upperBorder = keyValues.lastKey() + 5000;
      int toKey;
      if (upperBorder > 0)
        toKey = random.nextInt(upperBorder) - 5000;
      else
        toKey = random.nextInt(Integer.MAX_VALUE) - 5000;

      if (random.nextBoolean()) {
        Integer includedKey = keyValues.ceilingKey(toKey);
        if (includedKey != null)
          toKey = includedKey;
        else
          toKey = keyValues.floorKey(toKey);
      }

      int maxValuesToFetch = 10000;
      Collection<ORID> orids = sbTree.getValuesMinor(toKey, keyInclusive, maxValuesToFetch);

      Set<ORID> result = new HashSet<ORID>(orids);

      Iterator<ORID> valuesIterator = keyValues.headMap(toKey, keyInclusive).descendingMap().values().iterator();

      int fetchedValues = 0;
      while (valuesIterator.hasNext() && fetchedValues < maxValuesToFetch) {
        ORID value = valuesIterator.next();
        Assert.assertTrue(result.remove(value));

        fetchedValues++;
      }

      if (valuesIterator.hasNext())
        Assert.assertEquals(fetchedValues, maxValuesToFetch);

      Assert.assertEquals(result.size(), 0);
    }
  }

  private void assertBetweenValues(TreeMap<Integer, ORID> keyValues, MersenneTwisterFast random, boolean fromInclusive,
      boolean toInclusive) {
    for (int i = 0; i < 100; i++) {
      int upperBorder = keyValues.lastKey() + 5000;
      int fromKey;
      if (upperBorder > 0)
        fromKey = random.nextInt(upperBorder);
      else
        fromKey = random.nextInt(Integer.MAX_VALUE - 1);

      if (random.nextBoolean()) {
        Integer includedKey = keyValues.ceilingKey(fromKey);
        if (includedKey != null)
          fromKey = includedKey;
        else
          fromKey = keyValues.floorKey(fromKey);
      }

      int toKey = random.nextInt() + fromKey + 1;
      if (toKey < 0)
        toKey = Integer.MAX_VALUE;

      if (random.nextBoolean()) {
        Integer includedKey = keyValues.ceilingKey(toKey);
        if (includedKey != null)
          toKey = includedKey;
        else
          toKey = keyValues.floorKey(toKey);
      }

      if (fromKey > toKey)
        toKey = fromKey;

      int maxValuesToFetch = 10000;

      Collection<ORID> orids = sbTree.getValuesBetween(fromKey, fromInclusive, toKey, toInclusive, maxValuesToFetch);
      Set<ORID> result = new HashSet<ORID>(orids);

      Iterator<ORID> valuesIterator = keyValues.subMap(fromKey, fromInclusive, toKey, toInclusive).values().iterator();

      int fetchedValues = 0;
      while (valuesIterator.hasNext() && fetchedValues < maxValuesToFetch) {
        ORID value = valuesIterator.next();
        Assert.assertTrue(result.remove(value));

        fetchedValues++;
      }

      if (valuesIterator.hasNext())
        Assert.assertEquals(fetchedValues, maxValuesToFetch);

      Assert.assertEquals(result.size(), 0);
    }
  }
}
