package clpBasic.utilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RandomGeoMapper {
	private int gridSize;
	private int[][] grid;
	private int numCities;
	private int numCitiesOnRandomPath;			//Includes source station, min value = 2
	private Map<Integer, City> citiesMap;
	boolean localDebugMode = false;

	public RandomGeoMapper(int gridSize) {
		this.gridSize = gridSize;
		this.grid = new int[gridSize][gridSize];
		System.out.println(String.format("Grid of size %d x %d created successfully", gridSize, gridSize));
	}

	public void displayGrid() {
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				System.out.print(String.format("%d\t", grid[i][j]));
			}
			System.out.println();
		}
		System.out.println();
	}

	public void createRandomCitiesInGrid(int numCities) {
		if (numCities > gridSize * gridSize) {
			System.out.println(String.format("Error: numCities (%d) cannot be greater than gridSize*gridSize (%d)",
					numCities, gridSize * gridSize));
			System.exit(-1);
		}
		this.numCities = numCities;

		citiesMap = new LinkedHashMap<Integer, City>();
		for (int i = 0; i < numCities; i++) {
			int X = DataGenerator.getRandomIntInRange(0, gridSize - 1);
			int Y = DataGenerator.getRandomIntInRange(0, gridSize - 1);
			if (grid[X][Y] == 0) {
				grid[X][Y] = i + 1;
				citiesMap.put(i + 1, new City(i + 1, new Point2D(X, Y)));
			} else
				i--;
		}
		System.out.println(String.format("%d cities created successfully", numCities));
		displayGrid();

		if (localDebugMode) {
			for (Integer cityID : citiesMap.keySet()) {
				City c = citiesMap.get(cityID);
				System.out.println(String.format("CityID:%d\t%s", cityID, c.getLocation()));
			}
			System.out.println();
		}
	}

	public List<City> getRandomPath(int numCitiesOnRandomPath) {
		if (numCitiesOnRandomPath < 2) {
			System.out.println(
					String.format("Error: numCitiesOnRandomPath (%d) cannot be less than 2", numCitiesOnRandomPath));
			System.exit(-1);
		}
		if (numCitiesOnRandomPath > numCities) {
			System.out.println(String.format("Error: numCitiesOnRandomPath (%d) cannot be greater than numCities (%d)",
					numCitiesOnRandomPath, numCities));
			System.exit(-1);
		}
		this.numCitiesOnRandomPath = numCitiesOnRandomPath;

		System.out.println(String.format("Generating random path  with %d cities...", numCitiesOnRandomPath));
		List<City> citiesOnRandomPath = new ArrayList<City>();
		int currentCityID = 1;
		for (int i = 0; i < numCitiesOnRandomPath; i++) {
			City c1 = citiesMap.get(currentCityID);
			if (localDebugMode) {
				System.out.println(String.format("CurrentCityID:%-10d\t%s", c1.getCityID(), c1.getLocation()));
			}
			c1.setAddedToPath(true);
			c1.setPriorityOnPath(i);
			citiesOnRandomPath.add(c1);
			if (i == numCitiesOnRandomPath - 1)
				break;

			int nextCityID = 0;
			List<Point2D> nearByPoints = new ArrayList<Point2D>();
			nearByPoints.add(c1.getLocation().getShiftedPoint(-1, -1));
			nearByPoints.add(c1.getLocation().getShiftedPoint(0, -1));
			nearByPoints.add(c1.getLocation().getShiftedPoint(1, -1));
			nearByPoints.add(c1.getLocation().getShiftedPoint(-1, 0));
			nearByPoints.add(c1.getLocation().getShiftedPoint(1, 0));
			nearByPoints.add(c1.getLocation().getShiftedPoint(-1, 1));
			nearByPoints.add(c1.getLocation().getShiftedPoint(0, 1));
			nearByPoints.add(c1.getLocation().getShiftedPoint(1, 1));

			for (Point2D p : nearByPoints) {
				boolean isValidLocation = isLocationValid(p);

				boolean isValidCity = false;
				if (isValidLocation) {
					isValidCity = isCityIDValid(p);
				}

				boolean isCityAddedToPath = true;
				if (isValidCity) {
					isCityAddedToPath = checkCityVisitedStatus(p);
				}

//					System.out.println(String.format("%s\t%s\t%s\t%s", p, isValidLocation ? "Valid" : "Invalid",
//							isValidCity ? "City" : "Not City",
//							isCityAddedToPath ? "City Already Visited" : "Found City"));

				if (isValidLocation && isValidCity && !isCityAddedToPath) {
					nextCityID = grid[p.getX()][p.getY()];
					break;
				}
			}
			if (nextCityID > 0) {
				currentCityID = nextCityID;
			} else {
				for (Integer cityID : citiesMap.keySet()) {
					boolean isCityVisited = citiesMap.get(cityID).isAddedToPath();
//					System.out.println(String.format("cityID:%d\t%s", cityID, isCityVisited));
					if (!isCityVisited) {
						currentCityID = cityID;
						break;
					}
				}
			}
		}

		System.out.println();
		System.out.println("Cities On Random Path:");
		for (City c : citiesOnRandomPath) {
			System.out.println(c);
		}
		
		return citiesOnRandomPath;
	}

	private boolean isLocationValid(Point2D location) {
		return location.getX() >= 0 && location.getY() >= 0 && location.getX() < gridSize && location.getY() < gridSize;
	}

	private boolean isCityIDValid(Point2D location) {
		return grid[location.getX()][location.getY()] > 0;
	}

	private boolean checkCityVisitedStatus(Point2D point) {
		return citiesMap.get(grid[point.getX()][point.getY()]).isAddedToPath();
	}
}

class City {
	private int cityID;
	private Point2D location;
	private boolean addedToPath;
	private int priorityOnPath;

	public City(int cityID, Point2D location) {
		this.cityID = cityID;
		this.location = location;
	}

	public int getCityID() {
		return cityID;
	}

	public Point2D getLocation() {
		return location;
	}

	public boolean isAddedToPath() {
		return addedToPath;
	}

	public void setAddedToPath(boolean addedToPath) {
		this.addedToPath = addedToPath;
	}

	public int getPriorityOnPath() {
		return priorityOnPath;
	}

	public void setPriorityOnPath(int priorityOnPath) {
		this.priorityOnPath = priorityOnPath;
	}

	public String toString() {
		return String.format("CityID:%d\tLocation:%s\tPriority:%d", cityID, location, priorityOnPath);
	}
}

class Point2D {
	private int X, Y;

	public Point2D(int X, int Y) {
		this.X = X;
		this.Y = Y;
	}

	public int getX() {
		return X;
	}

	public int getY() {
		return Y;
	}

	public Point2D getShiftedPoint(int xShift, int yShift) {
		return new Point2D(X + xShift, Y + yShift);
	}

	public String toString() {
		return String.format("(%d,%d)", X, Y);
	}
}
