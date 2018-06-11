package org.mtransit.parser.ca_halifax_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTripStop;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.mt.data.MTrip;

// http://www.halifax.ca/opendata/
// http://www.halifax.ca/opendata/transit.php
// http://gtfs.halifax.ca/static/google_transit.zip
public class HalifaxTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-halifax-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new HalifaxTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Halifax Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Halifax Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

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

	private static final long RID_CP1 = 100001l;
	private static final long RID_ECRL = 100002l;
	private static final long RID_ECS = 100003l;
	private static final long RID_HWST = 100004l;
	private static final long RID_FV01 = 100101l;
	private static final long RID_MACK = 100005l;
	private static final long RID_MACD = 100006l;
	private static final long RID_S14 = 100114l;
	private static final long RID_SP6 = 100106l;
	private static final long RID_SP14 = 100014l;
	private static final long RID_SP53 = 100053l;
	private static final long RID_SP58 = 100058l;
	private static final long RID_SP65 = 100065l;

	@Override
	public long getRouteId(GRoute gRoute) {
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
			System.out.printf("\nUnexptected route ID for %s!\n", gRoute);
			System.exit(-1);
			return -1l;
		}
		System.out.printf("\nUnexpected route ID for %s!\n", gRoute);
		System.exit(-1);
		return -1l;
	}

	private static final Pattern STARTS_WITH_START = Pattern.compile("(\\* )", Pattern.CASE_INSENSITIVE);

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = STARTS_WITH_START.matcher(routeLongName).replaceAll(StringUtils.EMPTY);
		return routeLongName;
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return gRoute.getRouteShortName().toUpperCase(Locale.ENGLISH);
		}
		return super.getRouteShortName(gRoute);
	}

	private static final String AGENCY_COLOR = "FDB714"; // YELLOW (PDF map)

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String AGENCY_COLOR_BLUE = "08215C"; // BLUE (MetroLink SVG from Wikipedia)
	private static final String DEFAULT_ROUTE_COLOR = AGENCY_COLOR_BLUE;

	private static final String COLOR_EF4036 = "EF4036";
	private static final String COLOR_39B54A = "39B54A";
	private static final String COLOR_ED1C24 = "ED1C24";
	private static final String COLOR_1B75BC = "1B75BC";
	private static final String COLOR_F7931D = "F7931D";
	private static final String COLOR_669791 = "669791";
	private static final String COLOR_603812 = "603812";
	private static final String COLOR_D99937 = "D99937";
	private static final String COLOR_2BB673 = "2BB673";
	private static final String COLOR_834C90 = "834C90";
	private static final String COLOR_B93F25 = "B93F25";
	private static final String COLOR_5A4A42 = "5A4A42";
	private static final String COLOR_009344 = "009344";
	private static final String COLOR_A99E88 = "A99E88";
	private static final String COLOR_716558 = "716558";
	private static final String COLOR_008689 = "008689";
	private static final String COLOR_9B8578 = "9B8578";
	private static final String COLOR_00A79D = "00A79D";
	private static final String COLOR_91278F = "91278F";
	private static final String COLOR_79AD36 = "79AD36";
	private static final String COLOR_D91C5C = "D91C5C";
	private static final String COLOR_3B2314 = "3B2314";
	private static final String COLOR_F15A29 = "F15A29";
	private static final String COLOR_B9C21F = "B9C21F";
	private static final String COLOR_0099A3 = "0099A3";
	private static final String COLOR_754C28 = "754C28";
	private static final String COLOR_EE2A7B = "EE2A7B";
	private static final String COLOR_CC1F35 = "CC1F35";
	private static final String COLOR_235662 = "235662";
	private static final String COLOR_BE1E2D = "BE1E2D";
	private static final String COLOR_8CC63F = "8CC63F";
	private static final String COLOR_662D91 = "662D91";
	private static final String COLOR_A87B50 = "A87B50";
	private static final String COLOR_006738 = "006738";
	private static final String COLOR_EC008C = "EC008C";
	private static final String COLOR_2B3890 = "2B3890";
	private static final String COLOR_457E98 = "457E98";
	private static final String COLOR_003E43 = "003E43";
	private static final String COLOR_25AAE1 = "25AAE1";
	private static final String COLOR_B4451F = "B4451F";
	private static final String COLOR_262261 = "262261";
	private static final String COLOR_8B5D3B = "8B5D3B";

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return DEFAULT_ROUTE_COLOR;
		}
		int rsn = Integer.parseInt(gRoute.getRouteShortName());
		switch (rsn) {
		// @formatter:off
		case 1: return COLOR_EF4036;
		case 2: return COLOR_F15A29;
		case 4: return COLOR_39B54A;
		case 5: return COLOR_ED1C24;
		case 6: return COLOR_1B75BC;
		case 7: return COLOR_662D91;
		case 9: return COLOR_EE2A7B;
		case 10: return COLOR_1B75BC;
		case 11: return COLOR_BE1E2D;
		case 14: return COLOR_79AD36;
		case 15: return COLOR_F7931D;
		case 16: return COLOR_669791;
		case 17: return COLOR_603812;
		case 18: return COLOR_D99937;
		case 19: return COLOR_2BB673;
		case 20: return COLOR_F15A29;
		case 21: return COLOR_834C90;
		case 22: return COLOR_B93F25;
		case 23: return COLOR_754C28;
		case 31: return COLOR_B9C21F;
		case 32: return COLOR_662D91;
		case 33: return COLOR_5A4A42;
		case 34: return COLOR_009344;
		case 35: return COLOR_A99E88;
		case 41: return COLOR_B9C21F;
		case 42: return COLOR_006738;
		case 51: return COLOR_716558;
		case 52: return COLOR_008689;
		case 53: return COLOR_662D91;
		case 54: return COLOR_9B8578;
		case 55: return COLOR_F15A29;
		case 56: return COLOR_00A79D;
		case 57: return COLOR_91278F;
		case 58: return COLOR_25AAE1;
		case 59: return COLOR_79AD36;
		case 60: return COLOR_D91C5C;
		case 61: return COLOR_006738;
		case 62: return COLOR_3B2314;
		case 63: return COLOR_F15A29;
		case 64: return COLOR_B9C21F;
		case 65: return COLOR_0099A3;
		case 66: return COLOR_BE1E2D;
		case 68: return COLOR_754C28;
		case 72: return COLOR_EE2A7B;
		case 78: return COLOR_CC1F35;
		case 79: return COLOR_235662;
		case 80: return COLOR_BE1E2D;
		case 81: return COLOR_8CC63F;
		case 82: return COLOR_662D91;
		case 83: return COLOR_A87B50;
		case 84: return COLOR_25AAE1;
		case 85: return COLOR_006738;
		case 86: return COLOR_EC008C;
		case 87: return COLOR_2B3890;
		case 88: return COLOR_457E98;
		case 89: return COLOR_003E43;
		case 90: return COLOR_25AAE1;
		case 159: return DEFAULT_ROUTE_COLOR;
		case 185: return DEFAULT_ROUTE_COLOR;
		case 194: return "50ADE5";
		case 320: return DEFAULT_ROUTE_COLOR;
		case 330: return DEFAULT_ROUTE_COLOR;
		case 370: return DEFAULT_ROUTE_COLOR;
		case 400: return COLOR_B4451F;
		case 401: return COLOR_262261;
		case 402: return COLOR_8B5D3B;
		// @formatter:on
		default:
			if (isGoodEnoughAccepted()) {
				return null;
			}
			System.out.printf("\nUnexpected route color for %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
	}

	private static final String TO = " to ";
	private static final String VIA = " via ";
	private static final String SLASH = " / ";
	private static final String AND = " & ";
	private static final String HALIFAX_SHORT = "Hfx";
	private static final String TERMINAL = "Terminal";
	private static final String TERMINAL_SHORT = "Term";
	private static final String POINT_PLEASANT = "Pt Pleasant";
	private static final String WESTPHAL = "Westphal";
	private static final String DALHOUSIE = "Dalhousie";
	private static final String LACEWOOD = "Lacewood";
	private static final String LACEWOOD_TERMINAL = LACEWOOD + " " + TERMINAL;
	private static final String LACEWOOD_TERMINAL_SHORT = LACEWOOD + " " + TERMINAL_SHORT;
	private static final String SCOTIA_SQUARE = "Scotia Sq";
	private static final String MUMFORD = "Mumford";
	private static final String DOWNTOWN = "Downtown";
	private static final String DOWNTOWN_HALIFAX_SHORT = DOWNTOWN + " " + HALIFAX_SHORT;
	private static final String BURNSIDE = "Burnside";
	private static final String GARAGE = "Garage";
	private static final String BURNSIDE_GARAGE = BURNSIDE + " " + GARAGE;
	private static final String BRIDGE_TERMINAL = "Bridge " + TERMINAL;
	private static final String BRIDGE_TERMINAL_SHORT = "Bridge " + TERMINAL_SHORT;
	private static final String COLBY = "Colby";
	private static final String EASTERN_PASSAGE = "Eastern Passage";
	private static final String AUBURN = "Auburn";
	private static final String N_PRESTON = "N Preston";
	private static final String AUBURN_N_PRESTON = AUBURN + " - " + N_PRESTON;
	private static final String COBEQUID = "Cobequid";
	private static final String COBEQUID_TERMINAL = COBEQUID + " " + TERMINAL;
	private static final String HIGHFIELD = "Highfield";
	private static final String DARTMOUTH = "Dartmouth";
	private static final String DARTMOUTH_CROSSING = DARTMOUTH + " Xing";
	private static final String WOODSIDE_FERRY = "Woodside Ferry";
	private static final String TANTALLON = "Tantallon";
	private static final String JUNIOR_HIGH_SCHOOL = "Jr High";
	private static final String RAGGED_LAKE = "Ragged Lk";
	private static final String ILSLEY = "Ilsley";
	private static final String BAYERS = "Bayers";
	private static final String BAYERS_ROAD = BAYERS + " Rd";
	private static final String BAYERS_LAKE = BAYERS + " Lk";
	private static final String HIGHFIELD_TERMINAL = HIGHFIELD + " " + TERMINAL;
	private static final String MUMFORD_TERMINAL = MUMFORD + " " + TERMINAL;
	private static final String CUNARD_JUNIOR_HIGH_SCHOOL = "Cunard " + JUNIOR_HIGH_SCHOOL;
	private static final String EXHIBITION_PARK = "Exhibition Pk";
	private static final String WATER_ST_TERMINAL = "Water St " + TERMINAL;
	private static final String WATER_ST_TERMINAL_SHORT = "Water St " + TERMINAL_SHORT;
	private static final String LEIBLIN_PARK = "Leiblin Pk";
	private static final String SAINT_MARY_S = "St Mary's";
	private static final String SUMMER_STREET = "Summer St";
	private static final String TACOMA_CENTER = "Tacoma Ctr";
	private static final String MIC_MAC_TERMINAL = "Mic Mac " + TERMINAL;
	private static final String MICMAC_TERMINAL = "Micmac " + TERMINAL;
	private static final String PENHORN_TERMINAL = "Penhorn " + TERMINAL;
	private static final String PORTLAND_HILLS_TERMINAL = "Portland Hls " + TERMINAL;
	private static final String PORTLAND_HILLS_TERMINAL_SHORT = "Portland Hls " + TERMINAL_SHORT;
	private static final String TOWER_ROAD_LOOP = "Tower Rd Loop";
	private static final String OCEAN_BREEZE = "Ocean Breeze";
	private static final String MOUNT_ST_VINCENT = "Mt St Vincent";
	private static final String UNIVERSITIES = "Universities";
	private static final String HERITAGE_HILLS = "Heritage Hls";
	private static final String MONTAGUE_ROAD = "Montague Rd";
	private static final String SHELDRAKE_LAKE = "Sheldrake Lk";
	private static final String SUNNYSIDE = "Sunnyside";
	private static final String COLE_HARBOUR = "Cole Harbour";
	private static final String MOUNT_EDWARD = "Mt Edward";
	private static final String BUS_GARAGE = "Bus " + GARAGE;
	private static final String DENTITH = "Dentith";

	private static final String SOUTH_LC = "south";
	private static final String NORTH_LC = "north";
	private static final String EAST_LC = "east";
	private static final String WEST_LC = "west";

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(89L, new RouteTripSpec(89L, //
				1, MTrip.HEADSIGN_TYPE_STRING, COBEQUID_TERMINAL, // NORTH
				0, MTrip.HEADSIGN_TYPE_STRING, LACEWOOD_TERMINAL) // SOUTH
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"9102", // Lacewood Terminal Bay 2
								"6297", // Cobequid Terminal Bay 1
						})) //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"6297", // Cobequid Terminal Bay 1
								"9102", // Lacewood Terminal Bay 2
						})) //
				.compileBothTripSort());
		map2.put(RID_S14, new RouteTripSpec(RID_S14, //
				0, MTrip.HEADSIGN_TYPE_STRING, MUMFORD_TERMINAL, //
				1, MTrip.HEADSIGN_TYPE_STRING, CUNARD_JUNIOR_HIGH_SCHOOL) //
				.addTripSort(0, //
						Arrays.asList(new String[] { "8799", "8370", "7285" })) //
				.addTripSort(1, //
						Arrays.asList(new String[] { "8640", "7187", "8799" })) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public String cleanStopOriginalId(String gStopId) {
		gStopId = CleanUtils.cleanMergedID(gStopId);
		return gStopId;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		if (EAST_LC.equalsIgnoreCase(gTrip.getTripHeadsign())) {
			mTrip.setHeadsignDirection(MDirectionType.EAST);
			return;
		} else if (WEST_LC.equalsIgnoreCase(gTrip.getTripHeadsign())) {
			mTrip.setHeadsignDirection(MDirectionType.WEST);
			return;
		} else if (NORTH_LC.equalsIgnoreCase(gTrip.getTripHeadsign())) {
			mTrip.setHeadsignDirection(MDirectionType.NORTH);
			return;
		} else if (SOUTH_LC.equalsIgnoreCase(gTrip.getTripHeadsign())) {
			mTrip.setHeadsignDirection(MDirectionType.SOUTH);
			return;
		}
		String gTripHeadsign = gTrip.getTripHeadsign();
		if (gTripHeadsign.toLowerCase(Locale.ENGLISH).startsWith(mRoute.getShortName())) {
			gTripHeadsign = gTripHeadsign.substring(mRoute.getShortName().length() + 1);
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTripHeadsign), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 1l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL_SHORT, //
					SCOTIA_SQUARE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL_SHORT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 2l) {
			if (Arrays.asList( //
					LACEWOOD_TERMINAL, //
					MUMFORD_TERMINAL, //
					WATER_ST_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WATER_ST_TERMINAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4l) {
			if (Arrays.asList( //
					LACEWOOD_TERMINAL, //
					MUMFORD_TERMINAL, //
					WATER_ST_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WATER_ST_TERMINAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 5l) {
			if (Arrays.asList( //
					MUMFORD_TERMINAL, //
					WATER_ST_TERMINAL_SHORT //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WATER_ST_TERMINAL_SHORT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 6l) {
			if (Arrays.asList( //
					MUMFORD_TERMINAL, //
					WATER_ST_TERMINAL_SHORT //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WATER_ST_TERMINAL_SHORT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 9L) {
			if (Arrays.asList( //
					POINT_PLEASANT, //
					TOWER_ROAD_LOOP //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(POINT_PLEASANT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 9L + 1_000_000L) { // 9A
			if (Arrays.asList( //
					MUMFORD_TERMINAL, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 9L + 2_000_000L) { // 9B
			if (Arrays.asList( //
					"Fotherby", //
					"Dentith", //
					MUMFORD_TERMINAL, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 10l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					DALHOUSIE, //
					TACOMA_CENTER //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DALHOUSIE, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					MICMAC_TERMINAL, //
					MIC_MAC_TERMINAL, //
					SCOTIA_SQUARE, //
					WESTPHAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WESTPHAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 14L) {
			if (Arrays.asList( //
					MUMFORD_TERMINAL, //
					SCOTIA_SQUARE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SCOTIA_SQUARE, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DENTITH, //
					LEIBLIN_PARK, //
					MUMFORD_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(LEIBLIN_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 15l) {
			if (Arrays.asList( //
					BAYERS_ROAD, //
					MUMFORD_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BAYERS_ROAD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 18l) {
			if (Arrays.asList( //
					MOUNT_ST_VINCENT, //
					UNIVERSITIES, //
					SAINT_MARY_S //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SAINT_MARY_S, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 20l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					MUMFORD_TERMINAL//
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 21l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					LACEWOOD_TERMINAL//
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 22l) {
			if (Arrays.asList( //
					EXHIBITION_PARK, //
					RAGGED_LAKE//
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(EXHIBITION_PARK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 23l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					MUMFORD_TERMINAL//
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33L) {
			if (Arrays.asList( //
					LACEWOOD_TERMINAL, //
					SUMMER_STREET//
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SUMMER_STREET, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 41l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					SCOTIA_SQUARE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 51l) {
			if (Arrays.asList( //
					BURNSIDE, //
					OCEAN_BREEZE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BURNSIDE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 52l) {
			if (Arrays.asList( //
					BURNSIDE, //
					BURNSIDE_GARAGE, //
					ILSLEY, //
					LACEWOOD_TERMINAL_SHORT, //
					SCOTIA_SQUARE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BURNSIDE, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					BAYERS_LAKE, //
					BRIDGE_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BAYERS_LAKE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 53l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					SUMMER_STREET //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SUMMER_STREET, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 58l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					PENHORN_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 59l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					SUMMER_STREET, //
					PORTLAND_HILLS_TERMINAL, //
					PENHORN_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					COLBY, //
					PORTLAND_HILLS_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COLBY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 60l) {
			if (Arrays.asList( //
					EASTERN_PASSAGE, //
					HERITAGE_HILLS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(EASTERN_PASSAGE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 61l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					PORTLAND_HILLS_TERMINAL_SHORT, //
					SCOTIA_SQUARE, //
					MONTAGUE_ROAD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SCOTIA_SQUARE, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					AUBURN, //
					AUBURN_N_PRESTON, //
					N_PRESTON //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(N_PRESTON, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 66l) {
			if (Arrays.asList( //
					COBEQUID_TERMINAL, //
					HIGHFIELD_TERMINAL, //
					PENHORN_TERMINAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COBEQUID_TERMINAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 68l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					SUMMER_STREET //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 72l) {
			if (Arrays.asList( //
					BUS_GARAGE, //
					DARTMOUTH_CROSSING //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DARTMOUTH_CROSSING, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 78l) {
			if (Arrays.asList( //
					MOUNT_EDWARD, //
					WOODSIDE_FERRY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WOODSIDE_FERRY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 79l) {
			if (Arrays.asList( //
					COLE_HARBOUR, //
					WOODSIDE_FERRY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WOODSIDE_FERRY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 80l) {
			if (Arrays.asList( //
					COBEQUID_TERMINAL, //
					DOWNTOWN, //
					SUNNYSIDE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 82l) {
			if (Arrays.asList( //
					COBEQUID_TERMINAL, //
					WATER_ST_TERMINAL_SHORT //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WATER_ST_TERMINAL_SHORT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 320l) {
			if (Arrays.asList( //
					BRIDGE_TERMINAL, //
					DOWNTOWN_HALIFAX_SHORT //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN_HALIFAX_SHORT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 330l) {
			if (Arrays.asList( //
					SHELDRAKE_LAKE, //
					SHELDRAKE_LAKE + AND + TANTALLON, //
					TANTALLON //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SHELDRAKE_LAKE + AND + TANTALLON, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 370L) {
			if (Arrays.asList( //
					MICMAC_TERMINAL, //
					DOWNTOWN_HALIFAX_SHORT //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN_HALIFAX_SHORT, mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\n%s: Unexpected trips to merge: %s & %s!\n", mTrip.getRouteId(), mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^){1}(\\d+)(\\w?)(\\s){1}", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_TO = Pattern.compile("(^|\\s){1}(to)($|\\s){1}", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_ONLY = Pattern.compile("([\\s]*only[\\s]*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern METROLINK = Pattern.compile("((^|\\W){1}(metrolink)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	private static final Pattern EXPRESS = Pattern.compile("((^|\\W){1}(express)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	private static final Pattern CLEAN_WATER_ST_TERMINAL = Pattern.compile("((^|\\W){1}(water st[.]?[\\s]*terminal)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_WATER_ST_TERMINAL_REPLACEMENT = "$2" + WATER_ST_TERMINAL + "$4";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		int indexOfTO = tripHeadsign.indexOf(TO);
		if (indexOfTO >= 0) {
			tripHeadsign = tripHeadsign.substring(indexOfTO + TO.length());
		}
		int indexOfVIA = tripHeadsign.indexOf(VIA);
		if (indexOfVIA >= 0) {
			tripHeadsign = tripHeadsign.substring(0, indexOfVIA);
		}
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_ONLY.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = METROLINK.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CLEAN_WATER_ST_TERMINAL.matcher(tripHeadsign).replaceAll(CLEAN_WATER_ST_TERMINAL_REPLACEMENT);
		tripHeadsign = CleanUtils.SAINT.matcher(tripHeadsign).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern CLEAN_STREETS_CROSSING = Pattern.compile("((\\s)*(" //
			+ "after and opposite|afteropposite|after|" //
			+ "before and opposite|beforeopposite|before|" //
			+ "in front of|" //
			+ "opposite and after|opposite and before|" //
			+ "oppositeafter|oppositebefore|opposite" //
			+ ")(\\s)*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern CLEAN_CHEVRONS = Pattern.compile("(([^<]+)(<)([^>]+)(>))", Pattern.CASE_INSENSITIVE);
	private static final String CLEAN_CHEVRONS_REPLACEMENT = "$2$4";
	private static final Pattern CLEAN_BOUNDS = Pattern.compile("(\\[[^\\]]*\\])", Pattern.CASE_INSENSITIVE);
	private static final Pattern CIVIC_ADDRESS_ENDS_WITH = Pattern.compile("((\\s)*(" //
			+ "after|after and opposite|" //
			+ "before|before and opposite|" //
			+ "opposite and after|opposite and before|" //
			+ "opposite|in front of" //
			+ ")(\\s)*$)", Pattern.CASE_INSENSITIVE);
	private static final String STREET_NUMBER_SEPARATOR = ", ";
	private static final String CIVIC_ADDRESS = "civic address ";
	private static final Pattern ENDS_WITH_NUMBER = Pattern.compile("([ ]*\\([\\d]+\\)$)", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopName(String gStopName) {
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
	public int getStopId(GStop gStop) {
		if (Utils.isDigitsOnly(gStop.getStopId())) {
			return Integer.parseInt(gStop.getStopId());
		}
		Matcher matcher = DIGITS.matcher(gStop.getStopId());
		if (matcher.find()) {
			return Integer.parseInt(matcher.group());
		}
		System.out.printf("\nUnexpected stop ID for %s!\n", gStop);
		System.exit(-1);
		return -1;
	}

	@Override
	public String getStopCode(GStop gStop) {
		if (Utils.isDigitsOnly(gStop.getStopId())) {
			return gStop.getStopId(); // using stop ID as stop code ("GoTime" number)
		}
		Matcher matcher = DIGITS.matcher(gStop.getStopId());
		if (matcher.find()) {
			return matcher.group();
		}
		System.out.printf("\nUnexpected stop code for %s!\n", gStop);
		System.exit(-1);
		return null;
	}
}
