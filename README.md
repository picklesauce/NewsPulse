# Team-101-17

## Architecture

The app uses a layered architecture:

- **ui/** – Views (Compose screens, ViewModels, theme). Renders data and handles user input. Depends only on domain interfaces.
- **domain/** – Business objects (e.g. Article, LoginState) and interfaces for data access. No Android or Compose dependencies. Defines the contracts that the data layer implements.
- **data/** – Implementations that fetch or persist data (e.g. SharedPreferences wrappers, future API clients). Uses domain interfaces. The `data.mock` package holds mock implementations for testing.

## About the team

We are a group of CS students who are absolutely the best at building great projects. Look out for our CS346 project!

## The team members
* Balldeep Pannu
* Aadit Shah
* Ayaan Sarfraz
* Hanson Liu

## Useful Links
* [Team Contract](../../wikis/Team-Contract)
* [Project Proposal](../../wikis/Project-Proposal)
* [Team Meetings](../../wikis/Team-Meetings)

