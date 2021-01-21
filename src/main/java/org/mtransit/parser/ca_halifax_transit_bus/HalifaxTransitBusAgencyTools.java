package org.mtransit.parser.ca_halifax_transit_bus;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// http://www.halifax.ca/opendata/
// http://www.halifax.ca/opendata/transit.php
// http://gtfs.halifax.ca/static/google_transit.zip
public class HalifaxTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-halifax-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new HalifaxTransitBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIds;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Halifax Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Halifax Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTripInt(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String RTS_CP1 = "cp1";
	private static final String RTS_ECRL = "ecrl";
	private static final String RTS_ECS = "ecs";
	private static final String RTS_FV01 = "fv01";
	private static final String RTS_HWST = "hwst";
	private static final String RTS_MACK = "mack";
	private static final String RTS_MACD = "macd";
	private static final String RTS_S14 = "s14";
	private static final String RTS_SP6 = "sp6";
	private static final String RTS_SP14 = "sp14";
	private static final String RTS_SP53 = "sp53";
	private static final String RTS_SP58 = "sp58";
	private static final String RTS_SP65 = "sp65";

	private static final long RID_CP1 = 100_001L;
	private static final long RID_ECRL = 100_002L;
	private static final long RID_ECS = 100_003L;
	private static final long RID_HWST = 100_004L;
	private static final long RID_FV01 = 100_101L;
	private static final long RID_MACK = 100_005L;
	private static final long RID_MACD = 100_006L;
	private static final long RID_S14 = 100_114L;
	private static final long RID_SP6 = 100_106L;
	private static final long RID_SP14 = 100_014L;
	private static final long RID_SP53 = 100_053L;
	private static final long RID_SP58 = 100_058L;
	private static final long RID_SP65 = 100_065L;

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName()); // using route short name as route ID
		}
		if (RTS_CP1.equals(gRoute.getRouteShortName())) {
			return RID_CP1;
		} else if (RTS_ECRL.equals(gRoute.getRouteShortName())) {
			return RID_ECRL;
		} else if (RTS_ECS.equals(gRoute.getRouteShortName())) {
			return RID_ECS;
		} else if (RTS_FV01.equals(gRoute.getRouteShortName())) {
			return RID_FV01;
		} else if (RTS_HWST.equals(gRoute.getRouteShortName())) {
			return RID_HWST;
		} else if (RTS_MACK.equals(gRoute.getRouteShortName())) {
			return RID_MACK;
		} else if (RTS_MACD.equals(gRoute.getRouteShortName())) {
			return RID_MACD;
		} else if (RTS_S14.equals(gRoute.getRouteShortName())) {
			return RID_S14;
		} else if (RTS_SP6.equals(gRoute.getRouteShortName())) {
			return RID_SP6;
		} else if (RTS_SP14.equals(gRoute.getRouteShortName())) {
			return RID_SP14;
		} else if (RTS_SP53.equals(gRoute.getRouteShortName())) {
			return RID_SP53;
		} else if (RTS_SP58.equals(gRoute.getRouteShortName())) {
			return RID_SP58;
		} else if (RTS_SP65.equals(gRoute.getRouteShortName())) {
			return RID_SP65;
		}
		Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
		if (matcher.find()) {
			int digits = Integer.parseInt(matcher.group());
			String rsn = gRoute.getRouteShortName().toLowerCase(Locale.ENGLISH);
			if (rsn.endsWith("a")) {
				return digits + 1_000_000L;
			} else if (rsn.endsWith("b")) {
				return digits + 2_000_000L;
			}
			throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
		}
		throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
	}

	private static final Pattern STARTS_WITH_START = Pattern.compile("(\\* )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = STARTS_WITH_START.matcher(routeLongName).replaceAll(StringUtils.EMPTY);
		return routeLongName;
	}

	@Nullable
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return gRoute.getRouteShortName().toUpperCase(Locale.ENGLISH);
		}
		return super.getRouteShortName(gRoute);
	}

	private static final String AGENCY_COLOR = "FDB714"; // YELLOW (PDF map)

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String AGENCY_COLOR_BLUE = "08215C"; // BLUE (MetroLink SVG from Wikipedia)
	private static final String DEFAULT_ROUTE_COLOR = AGENCY_COLOR_BLUE;

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return DEFAULT_ROUTE_COLOR;
		}
		int rsn = Integer.parseInt(gRoute.getRouteShortName());
		switch (rsn) {
		// @formatter:off
		case 1: return "EF4036";
		case 2: return "F15A29";
		case 3: return null; // TODO
		case 4: return "39B54A";
		case 5: return "ED1C24";
		case 6: return "1B75BC";
		case 7: return "662D91";
		case 8: return null; // TODO
		case 9: return "EE2A7B";
		case 10: return "1B75BC";
		case 11: return "BE1E2D";
		case 14: return "79AD36";
		case 15: return "F7931D";
		case 16: return "669791";
		case 17: return "603812";
		case 18: return "D99937";
		case 19: return "2BB673";
		case 20: return "F15A29";
		case 21: return "834C90";
		case 22: return "B93F25";
		case 23: return "754C28";
		case 25: return null; // TODO
		case 28: return null; // TODO
		case 29: return null; // TODO
		case 31: return "B9C21F";
		case 32: return "662D91";
		case 33: return "5A4A42";
		case 34: return "009344";
		case 35: return "A99E88";
		case 39: return null; // TODO
		case 41: return "B9C21F";
		case 42: return "006738";
		case 51: return "716558";
		case 52: return "008689";
		case 53: return "662D91";
		case 54: return "9B8578";
		case 55: return "F15A29";
		case 56: return "00A79D";
		case 57: return "91278F";
		case 58: return "25AAE1";
		case 59: return "79AD36";
		case 60: return "D91C5C";
		case 61: return "006738";
		case 62: return "3B2314";
		case 63: return "F15A29";
		case 64: return "B9C21F";
		case 65: return "0099A3";
		case 66: return "BE1E2D";
		case 68: return "754C28";
		case 72: return "EE2A7B";
		case 78: return "CC1F35";
		case 79: return "235662";
		case 80: return "BE1E2D";
		case 81: return "8CC63F";
		case 82: return "662D91";
		case 83: return "A87B50";
		case 84: return "25AAE1";
		case 85: return "006738";
		case 86: return "EC008C";
		case 87: return "2B3890";
		case 88: return "457E98";
		case 89: return "003E43";
		case 90: return "25AAE1";
		case 91: return null; // TODO
		case 93: return null; // TODO
		case 123: return null; // TODO
		case 135: return null; // TODO
		case 136: return null; // TODO
		case 137: return null; // TODO
		case 138: return null; // TODO
		case 159: return DEFAULT_ROUTE_COLOR;
		case 185: return DEFAULT_ROUTE_COLOR;
		case 182: return null; // TODO
		case 183: return null; // TODO
		case 186: return null; // TODO
		case 194: return "50ADE5";
		case 196: return null; // TODO
		case 320: return DEFAULT_ROUTE_COLOR;
		case 330: return DEFAULT_ROUTE_COLOR;
		case 370: return DEFAULT_ROUTE_COLOR;
		case 400: return "B4451F";
		case 401: return "262261";
		case 402: return "8B5D3B";
		case 415: return null; // TODO
		case 433: return null; // TODO
		// @formatter:on
		default:
			throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
		}
	}

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = CleanUtils.cleanMergedID(gStopId);
		return gStopId;
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("%s: Unexpected trips to merge: %s & %s!", mTrip.getRouteId(), mTrip, mTripToMerge);
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^)(\\d+)(\\w?)(\\s)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_ONLY = Pattern.compile("([\\s]*only[\\s]*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern METROLINK = Pattern.compile("((^|\\W)(metrolink)(\\W|$))", Pattern.CASE_INSENSITIVE);

	private static final Pattern EXPRESS = Pattern.compile("((^|\\W)(express)(\\W|$))", Pattern.CASE_INSENSITIVE);

	private static final String TERMINAL_SHORT = "Term";
	private static final Pattern TERMINAL_ = Pattern.compile("((^|\\W)(terminal)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String TERMINAL_REPLACEMENT = "$2" + TERMINAL_SHORT + "$4";

	private static final String WATER_ST_TERMINAL = "Water St " + TERMINAL_SHORT;
	private static final Pattern WATER_ST_TERMINAL_ = Pattern.compile("((^|\\W)(water st[.]?[\\s]*term)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String WATER_ST_TERMINAL_REPLACEMENT = "$2" + WATER_ST_TERMINAL + "$4";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_ONLY.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = METROLINK.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = TERMINAL_.matcher(tripHeadsign).replaceAll(TERMINAL_REPLACEMENT);
		tripHeadsign = WATER_ST_TERMINAL_.matcher(tripHeadsign).replaceAll(WATER_ST_TERMINAL_REPLACEMENT);
		tripHeadsign = CleanUtils.SAINT.matcher(tripHeadsign).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final String SLASH = " / ";
	private static final Pattern CLEAN_STREETS_CROSSING = Pattern.compile("((\\s)*(" //
			+ "after and opposite|afteropposite|after|" //
			+ "before and opposite|beforeopposite|before|" //
			+ "in front of|" //
			+ "opposite and after|opposite and before|" //
			+ "oppositeafter|oppositebefore|opposite" //
			+ ")(\\s)*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern CLEAN_CHEVRONS = Pattern.compile("(([^<]+)(<)([^>]+)(>))", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_CHEVRONS_REPLACEMENT = "$2$4";
	private static final Pattern CLEAN_BOUNDS = Pattern.compile("(\\[[^]]*])", Pattern.CASE_INSENSITIVE);
	private static final Pattern CIVIC_ADDRESS_ENDS_WITH = Pattern.compile("((\\s)*(" //
			+ "after|after and opposite|" //
			+ "before|before and opposite|" //
			+ "opposite and after|opposite and before|" //
			+ "opposite|in front of" //
			+ ")(\\s)*$)", Pattern.CASE_INSENSITIVE);
	private static final String STREET_NUMBER_SEPARATOR = ", ";
	private static final String CIVIC_ADDRESS = "civic address ";
	private static final Pattern ENDS_WITH_NUMBER = Pattern.compile("([ ]*\\([\\d]+\\)$)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		if (Utils.isUppercaseOnly(gStopName, true, true)) {
			gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		}
		int indexOfCivicAddress = gStopName.toLowerCase(Locale.ENGLISH).indexOf(CIVIC_ADDRESS);
		if (indexOfCivicAddress >= 0) {
			gStopName = gStopName.substring(indexOfCivicAddress + CIVIC_ADDRESS.length()) + STREET_NUMBER_SEPARATOR
					+ gStopName.substring(0, indexOfCivicAddress);
			gStopName = CIVIC_ADDRESS_ENDS_WITH.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		}
		gStopName = CLEAN_BOUNDS.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CLEAN_CHEVRONS.matcher(gStopName).replaceAll(CLEAN_CHEVRONS_REPLACEMENT);
		gStopName = CLEAN_STREETS_CROSSING.matcher(gStopName).replaceAll(SLASH);
		gStopName = ENDS_WITH_NUMBER.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId = gStop.getStopId();
		if (Utils.isDigitsOnly(stopId)) {
			return Integer.parseInt(stopId);
		}
		Matcher matcher = DIGITS.matcher(stopId);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group());
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId = gStop.getStopId();
		if (Utils.isDigitsOnly(stopId)) {
			return stopId; // using stop ID as stop code ("GoTime" number)
		}
		Matcher matcher = DIGITS.matcher(stopId);
		if (matcher.find()) {
			return matcher.group();
		}
		throw new MTLog.Fatal("Unexpected stop code for %s!", gStop);
	}
}
