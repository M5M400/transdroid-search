/*
 *	This file is part of Transdroid Torrent Search 
 *	<http://code.google.com/p/transdroid-search/>
 *	
 *	Transdroid Torrent Search is free software: you can redistribute 
 *	it and/or modify it under the terms of the GNU Lesser General 
 *	Public License as published by the Free Software Foundation, 
 *	either version 3 of the License, or (at your option) any later 
 *	version.
 *	
 *	Transdroid Torrent Search is distributed in the hope that it will 
 *	be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *	warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 *	See the GNU Lesser General Public License for more details.
 *	
 *	You should have received a copy of the GNU Lesser General Public 
 *	License along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transdroid.search;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import android.util.Pair;
import org.transdroid.search.gui.SettingsHelper;

import java.util.List;

/**
 * Provider of a list of available torrent sites.
 * 
 * @author Eric Taix
 * @author Eric Kok
 */
public class TorrentSitesProvider extends ContentProvider {

	public static final String PROVIDER_NAME = "org.transdroid.search.torrentsitesprovider";

	/** The content URI to use. Useful if the application has access to this class. Otherwise it must build the URI like<br/>
	   <code>Uri uri = Uri.parse("content://org.transdroid.search.torrentsitesprovider/sites");</code><br/>
	   And within an activity then call:<br/>
	   <code>Cursor cur = managedQuery(uri, null, null, null, null);</code>
	 **/
	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/sites");

	/*
	 * Not supported by this content provider
	 */
	@Override
	public int delete(Uri uriP, String selectionP, String[] selectionArgsP) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uriP) {
		return "vnd.android.cursor.dir/vnd.transdroid.torrentsite";
	}

	/*
	 * Not supported by this content provider
	 */
	@Override
	public Uri insert(Uri uriP, ContentValues valuesP) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String,
	 * java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uriP, String[] projectionP, String selectionP, String[] selectionArgsP, String sortOrderP) {

		Log.d(TorrentSitesProvider.class.toString(), "List all enabled sites");
		
		// The available columns; note that an _ID is a ContentProvider-requirement
		String[] columnNames = new String[] { "_ID", "CODE", "NAME", "RSSURL", "ISPRIVATE" };
		MatrixCursor curs = new MatrixCursor(columnNames);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		List<Pair<String, ISearchAdapter>> sites = SettingsHelper.getAllSites(prefs);

		// Return the enabled results as MatrixCursor
		int id = 0;
		for (Pair<String, ISearchAdapter> site : sites) {
			
			// Don't include this site if the user disabled it (or it is private and no credentials are specified)
			if (!SettingsHelper.isSiteEnabled(prefs, site.first, site.second))
				continue;
			
			Object[] values = new Object[5];
			values[0] = id++;
			values[1] = site.first;
			values[2] = site.second.getSiteName();
			values[3] = site.second.buildRssFeedUrlFromSearch(prefs, "%s", SortOrder.BySeeders);
			values[4] = site.second.getAuthType().ordinal();
			curs.addRow(values);
			
		}

		// Register to watch a content URI for changes (don't really know what it means ?)
		curs.setNotificationUri(getContext().getContentResolver(), uriP);
		return curs;
	}

	/*
	 * Not supported by this content provider
	 */
	@Override
	public int update(Uri uriP, ContentValues valuesP, String selectionP, String[] selectionArgsP) {
		throw new UnsupportedOperationException();
	}

}
