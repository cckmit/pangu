package com.pangu.framework.utils.geohash;

import com.pangu.framework.utils.geohash.util.VincentyGeodesy;

public class GoogleDistance {

  private static final double EARTH_RADIUS = 6378.137;

  private static double rad(double d){
      return d * Math.PI / 180.0;
  }
  
  /**
   * 计算两个经纬度间的距离(km)
   * long1 位置1经度
   * lat1  位置1纬度
   * long2 位置2经度
   * lat2  位置2纬度
   * @return
   */
  public static double getDistance(double long1, double lat1, double long2, double lat2) {
      double a, b, d, sa2, sb2;
      lat1 = rad(lat1);
      lat2 = rad(lat2);
      a = lat1 - lat2;
      b = rad(long1 - long2);

      sa2 = Math.sin(a / 2.0);
      sb2 = Math.sin(b / 2.0);
      d = 2   * EARTH_RADIUS
              * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)
              * Math.cos(lat2) * sb2 * sb2));
      return d;
  }

  public static void main(String[] args) {
	  double long1 = 113.3271, lat1 = 23.1376, long2 = 113.26438499999, lat2 = 23.12911;
	  GeoHash one = GeoHash.withCharacterPrecision(lat1, long1, 12);
	  GeoHash two = GeoHash.withCharacterPrecision(lat2, long2, 12);
      double geohashDistance = VincentyGeodesy.distanceInMeters(one.getPoint(), two.getPoint());
      double googleDistance = getDistance(long1, lat1, long2, lat2);
	  System.err.println("WGS84  : " + geohashDistance / 1000 + " km");
      System.err.println("google : " + googleDistance + " km");
  }
}
