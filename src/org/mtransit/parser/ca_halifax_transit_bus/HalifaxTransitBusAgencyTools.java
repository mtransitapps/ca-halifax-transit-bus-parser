package org.mtransit.parser.ca_halifax_transit_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.mt.data.MTrip;

// http://www.halifax.ca/opendata/
// http://www.halifax.ca/metrotransit/googletransitfeed/google_transit.zip
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
		System.out.println("Unexpected route ID " + gRoute);
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
		case 320: return DEFAULT_ROUTE_COLOR;
		case 330: return DEFAULT_ROUTE_COLOR;
		case 370: return DEFAULT_ROUTE_COLOR;
		case 400: return COLOR_B4451F;
		case 401: return COLOR_262261;
		case 402: return COLOR_8B5D3B;
		// @formatter:on
		default:
			System.out.println("Unexpected route color " + gRoute);
			System.exit(-1);
			return null;
		}
	}

	private static final String TO = " to ";
	private static final String VIA = " via ";

	private static final String POINT_PLEASANT = "Pt Pleasant";
	private static final String WESTPHAL = "Westphal";
	private static final String DALHOUSIE = "Dalhousie";
	private static final String LACEWOOD = "Lacewood";
	private static final String SCOTIA_SQUARE = "Scotia Sq";
	private static final String MUMFORD = "Mumford";
	private static final String UNIVERSITIES = "Universities";
	private static final String MUMFORD_DOWNTOWN = "Mumford-Downtown";
	private static final String LACEWOOD_HALIFAX = "Lacewood-Halifax";
	private static final String MUMFORD_HALIFAX = "Mumford-Halifax";
	private static final String DOWNTOWN = "Downtown";
	private static final String OCEAN_BREEZE_BURNSIDE = "Ocean Breeze-Burnside";
	private static final String LACEWOOD_CHAIN_LAKE_DRIVE = "Lacewood-Chain Lk Dr";
	private static final String BRIDGE_TERMINAL_BURNSIDE = "Bridge Terminal-Burnside";
	private static final String COLBY_VILLAGE = "Colby Vlg";
	private static final String EASTERN_PASSAGE_HERITAGE_HILLS = "Eastern Passage-Heritage Hls";
	private static final String BRIDGE_TERMINAL_HALIFAX = "Bridge Terminal-Halifax";
	private static final String FOREST_HILLS_NORTH_PRESTON = "Forest Hills-North Preston";
	private static final String HIGHFIELD_COBEQUID = "Highfield-Cobequid";
	private static final String BRIDGE_TERMINAL = "Bridge Terminal";
	private static final String DARTMOUTH_CROSSING = "Dartmouth Crossing";
	private static final String WOODSIDE_FERRY = "Woodside Ferry";
	private static final String HALIFAX = "Halifax";
	private static final String SACKVILLE = "Sackville";
	private static final String TANTALLON = "Tantallon";
	private static final String DUNBRACK_ST_LANGBRAE_DR = "Dunbrack St / Langbrae Dr";
	private static final String CLAYTON_PARK_JUNIOR_HIGH = "Clayton Pk Junior High";
	private static final String RAGGED_LAKE_TRANSIT_CENTRE = "Ragged Lake Transit Ctr";
	private static final String ILSLEY_AVE = "Ilsley Ave";
	private static final String DARTMOUTH_BRIDGE_TERMINAL = "Dartmouth Bridge Terminal";
	private static final String HALIFAX_WEST_HIGH_SCHOOL = "Halifax West High School";
	private static final String HERRING_COVE_RD = "Herring Cv Rd";
	private static final String DARTMOUTH_HIGH_SCHOOL = "Dartmouth High School";
	private static final String HIGHFIELD_TERMINAL = "Highfield Terminal";
	private static final String FAIRVIEW_JUNIOR_HIGH_SCHOOL = "Fairview Junior High School";
	private static final String PARKLAND_DR = "Parkland Dr";
	private static final String MUMFORD_TERMINAL = "Mumford Terminal";
	private static final String CUNARD_JUNIOR_HIGH_SCHOOL = "Cunard Junior High School";
	private static final String SPRINGFIELD = "Springfield";
	private static final String SACKVILLE_TERMINAL = "Sackville Terminal";
	private static final String CITADEL_HIGH_SCHOOL = "Citadel High School";
	private static final String ASTRAL_JUNIOR_HIGH_SCH = "Astral Junior High School";
	private static final String PRINCE_ANDREW_HIGH_SCHOOL = "Prince Andrew High School";
	private static final String EXHIBITION_PARK = "Exhibition Pk";
	private static final String PORTLAND_HLS = "Portland Hls";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.id == 1l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 2l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 4l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 5l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 6l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 9l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(POINT_PLEASANT, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 10l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(WESTPHAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 14l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SCOTIA_SQUARE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 15l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MUMFORD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 16l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(LACEWOOD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 18l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(UNIVERSITIES, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 20l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MUMFORD_DOWNTOWN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 21l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(LACEWOOD_HALIFAX, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 22l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MUMFORD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(EXHIBITION_PARK, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 23l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MUMFORD_HALIFAX, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 33l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 42l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(LACEWOOD, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DALHOUSIE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 51l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(OCEAN_BREEZE_BURNSIDE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 52l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(LACEWOOD_CHAIN_LAKE_DRIVE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL_BURNSIDE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 53l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL_HALIFAX, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 58l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 59l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL_HALIFAX, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(COLBY_VILLAGE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 60l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(EASTERN_PASSAGE_HERITAGE_HILLS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 61l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL_HALIFAX, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(FOREST_HILLS_NORTH_PRESTON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 66l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HIGHFIELD_COBEQUID, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 68l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(BRIDGE_TERMINAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 72l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DARTMOUTH_CROSSING, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 78l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WOODSIDE_FERRY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 79l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(WOODSIDE_FERRY, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 80l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HALIFAX, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 82l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HALIFAX, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 83l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(SACKVILLE_TERMINAL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SPRINGFIELD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 159l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HALIFAX, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(PORTLAND_HLS, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 185l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HALIFAX, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(SACKVILLE, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 320l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(HALIFAX, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == 330l) {
			if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(TANTALLON, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_CP1) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DUNBRACK_ST_LANGBRAE_DR, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CLAYTON_PARK_JUNIOR_HIGH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_ECRL) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(RAGGED_LAKE_TRANSIT_CENTRE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(MUMFORD_TERMINAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_ECS) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ILSLEY_AVE, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(DARTMOUTH_BRIDGE_TERMINAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_HWST) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MUMFORD_TERMINAL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HALIFAX_WEST_HIGH_SCHOOL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_MACK) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.id == RID_SP14) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MUMFORD_TERMINAL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HERRING_COVE_RD, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_SP53) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(DARTMOUTH_HIGH_SCHOOL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(HIGHFIELD_TERMINAL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_SP58) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(PRINCE_ANDREW_HIGH_SCHOOL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_SP65) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(ASTRAL_JUNIOR_HIGH_SCH, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_FV01) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(FAIRVIEW_JUNIOR_HIGH_SCHOOL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(PARKLAND_DR, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_SP6) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(CITADEL_HIGH_SCHOOL, gTrip.getDirectionId());
				return;
			}
		} else if (mRoute.id == RID_S14) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignString(MUMFORD_TERMINAL, gTrip.getDirectionId());
				return;
			} else if (gTrip.getDirectionId() == 1) {
				mTrip.setHeadsignString(CUNARD_JUNIOR_HIGH_SCHOOL, gTrip.getDirectionId());
				return;
			}
		}
		String gTripHeadsign = gTrip.getTripHeadsign();
		if (gTripHeadsign.toLowerCase(Locale.ENGLISH).startsWith(mRoute.shortName)) {
			gTripHeadsign = gTripHeadsign.substring(mRoute.shortName.length() + 1);
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTripHeadsign), gTrip.getDirectionId());
	}

	private static final Pattern STARTS_WITH_TO = Pattern.compile("(^|\\s){1}(to)($|\\s){1}", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		int indexOfTO = tripHeadsign.indexOf(TO);
		if (indexOfTO >= 0) {
			tripHeadsign = tripHeadsign.substring(indexOfTO + TO.length());
		}
		int indexOfVIA = tripHeadsign.indexOf(VIA);
		if (indexOfVIA >= 0) {
			tripHeadsign = tripHeadsign.substring(0, indexOfVIA);
		}
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final String SLASH = " / ";
	private static final Pattern CLEAN_STREETS_CROSSING = Pattern.compile("((\\s)*(" //
			+ "after and opposite|afteropposite|after|" //
			+ "before and opposite|beforeopposite|before|" //
			+ "in front of|" //
			+ "oppositeafter|oppositebefore|opposite" //
			+ ")(\\s)*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern CLEAN_BOUNDS = Pattern.compile("(\\[[^\\]]*\\])", Pattern.CASE_INSENSITIVE);
	private static final Pattern CIVIC_ADDRESS_ENDS_WITH = Pattern.compile(
			"((\\s)*(after|after and opposite|before|before and opposite|opposite|in front of)(\\s)*$)", Pattern.CASE_INSENSITIVE);
	private static final String STREET_NUMBER_SEPARATOR = ", ";
	private static final String CIVIC_ADDRESS = "civic address ";

	@Override
	public String cleanStopName(String gStopName) {
		int indexOfCivicAddress = gStopName.toLowerCase(Locale.ENGLISH).indexOf(CIVIC_ADDRESS);
		if (indexOfCivicAddress >= 0) {
			gStopName = gStopName.substring(indexOfCivicAddress + CIVIC_ADDRESS.length()) + STREET_NUMBER_SEPARATOR
					+ gStopName.substring(0, indexOfCivicAddress);
			gStopName = CIVIC_ADDRESS_ENDS_WITH.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		}
		gStopName = CLEAN_BOUNDS.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CLEAN_STREETS_CROSSING.matcher(gStopName).replaceAll(SLASH);
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
		matcher.find();
		return Integer.parseInt(matcher.group());
	}

	@Override
	public String getStopCode(GStop gStop) {
		if (Utils.isDigitsOnly(gStop.getStopId())) {
			return gStop.getStopId(); // using stop ID as stop code ("GoTime" number)
		}
		Matcher matcher = DIGITS.matcher(gStop.getStopId());
		matcher.find();
		return matcher.group();
	}
}
