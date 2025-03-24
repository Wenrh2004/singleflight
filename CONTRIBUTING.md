# Contributing to SingleFlight

Thank you for your interest in contributing to SingleFlight! This document provides guidelines and instructions for contributing to this project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Development Environment Setup](#development-environment-setup)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## Code of Conduct

By participating in this project, you are expected to uphold our Code of Conduct:

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## Development Environment Setup

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.6 or higher
- Git

### Setting Up the Project

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/singleflight.git
   cd singleflight
   ```
3. Add the original repository as an upstream remote:
   ```bash
   git remote add upstream https://github.com/Wenrh2004/singleflight.git
   ```
4. Build the project using Maven:
   ```bash
   mvn clean install
   ```

## Coding Standards

Please follow these coding standards when contributing to SingleFlight:

### Java Code Style

- Use 4 spaces for indentation (not tabs)
- Follow Java naming conventions:
  - `CamelCase` for class names
  - `camelCase` for method and variable names
  - `UPPER_SNAKE_CASE` for constants
- Maximum line length of 120 characters
- Always add appropriate Javadoc comments for public methods and classes
- Use meaningful variable and method names

### Documentation

- Update documentation when changing functionality
- Write clear and concise commit messages
- Include examples for new features

## Testing

All contributions should include appropriate tests:

- Write unit tests for new functionality
- Ensure all tests pass before submitting a pull request
- Aim for high test coverage of your code
- Run tests using Maven:
  ```bash
  mvn test
  ```

## Pull Request Process

1. Update your fork to the latest upstream version
2. Create a new branch for your feature or bugfix:
   ```bash
   git checkout -b feature/your-feature-name
   ```
   or
   ```bash
   git checkout -b fix/issue-you-are-fixing
   ```
3. Make your changes and commit them with clear, descriptive messages
4. Merge the latest changes from the upstream repository:
   ```bash
   git fetch upstream
   git merge upstream/main
   ```
5. Commit your changes:
   ```bash
   git commit -m "<type>(<scope>): <subject>"
   ``` 
   where `<type>` can be `feat`, `fix`, `docs`, `style`, `refactor`, `test`, or `chore`, and `<scope>` is optional.
6. Push your branch to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
5. Open a pull request against the main repository
6. Ensure the PR description clearly describes the problem and solution
7. Reference any relevant issues in your PR description

### Pull Request Review Criteria

Before your pull request can be merged, it will be reviewed for:

- Code quality and style
- Test coverage
- Documentation
- Compatibility with existing code

## Issue Reporting

When reporting issues, please include:

- A clear and descriptive title
- A detailed description of the issue
- Steps to reproduce the problem
- Expected behavior
- Actual behavior
- Environment information (JDK version, OS, etc.)
- Any relevant logs or screenshots

---

Thank you for contributing to SingleFlight! Your efforts help make this project better for everyone.