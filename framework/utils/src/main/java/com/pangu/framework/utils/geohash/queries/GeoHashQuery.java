package com.pangu.framework.utils.geohash.queries;

import java.util.List;

import com.pangu.framework.utils.geohash.GeoHash;
import com.pangu.framework.utils.geohash.WGS84Point;

public interface GeoHashQuery {

	/**
	 * check wether a geohash is within the hashes that make up this query.
	 */
	public boolean contains(GeoHash hash);

	/**
	 * returns whether a point lies within a query.
	 */
	public boolean contains(WGS84Point point);

	/**
	 * should return the hashes that re required to perform this search.
	 */
	public List<GeoHash> getSearchHashes();

	public String getWktBox();

}