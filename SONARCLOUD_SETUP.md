# SonarQube Cloud Integration Guide

This document provides step-by-step instructions to set up SonarQube Cloud for the MediBook Backend project.

## 📋 Prerequisites

- SonarQube Cloud account (https://sonarcloud.io)
- GitHub account with this repository
- Maven 3.6+
- Java 21

## 🚀 Setup Instructions

### Step 1: Create SonarQube Cloud Account & Project

1. Go to [SonarQube Cloud](https://sonarcloud.io)
2. Sign up with your GitHub account
3. Create a new organization or use existing one
4. Create a new project or import from GitHub

### Step 2: Get Your Project Keys

After creating your project in SonarQube Cloud, you'll need:

1. **SONAR_PROJECT_KEY**: Found in your project settings
   - Navigate to: Project Settings > General
   - Copy the project key

2. **SONAR_ORGANIZATION**: Your organization key
   - Navigate to: Settings > Organization
   - Copy the organization key

3. **SONAR_TOKEN**: Generate a token for authentication
   - Navigate to: Account > Security > Generate Tokens
   - Name it "GitHub-MediBook" or similar
   - Copy the token (keep it safe!)

### Step 3: Add GitHub Secrets

Add the following secrets to your GitHub repository:

1. Go to: **GitHub Repository > Settings > Secrets and variables > Actions**

2. Create the following secrets:
   - **SONAR_TOKEN**: Paste your SonarQube Cloud token
   - **SONAR_PROJECT_KEY**: Paste your project key
   - **SONAR_ORGANIZATION**: Paste your organization key

### Step 4: Update Configuration Files

The following files have been created with placeholder values:

#### `sonar-project.properties`
- Replace `your_project_key` with your actual project key
- Replace `your_organization` with your actual organization key

#### `pom.xml` (root)
- Update the properties section with your project details:
  ```xml
  <sonar.projectKey>your_project_key</sonar.projectKey>
  <sonar.organization>your_organization</sonar.organization>
  ```

#### `.github/workflows/sonarcloud.yml`
- This is already configured to use GitHub secrets
- No changes needed unless you want to customize the workflow

### Step 5: Verify Your Service pom.xml Files

Each microservice should have a parent reference to the root pom.xml. Update service pom files if needed:

**Example for api-gateway/pom.xml:**
```xml
<parent>
    <groupId>com.medibook</groupId>
    <artifactId>medibook-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
</parent>
```

## 📁 Project Structure

```
MediBook/
├── pom.xml                    # Root POM with SonarQube config
├── sonar-project.properties   # SonarQube project settings
├── .github/
│   └── workflows/
│       └── sonarcloud.yml     # GitHub Actions workflow
├── api-gateway-service/
│   └── api-gateway/
│       └── pom.xml
├── auth-service/
│   └── auth-service/
│       └── pom.xml
├── provider-service/
│   └── provider-service/
│       └── pom.xml
├── appointment-service/
│   └── appointment-service/
│       └── pom.xml
└── payment-service/
    └── payment-service/
        └── pom.xml
```

## 🧪 Local Analysis (Optional)

To run SonarQube analysis locally:

```bash
# Clean build with coverage
mvn clean verify

# Run SonarCloud analysis
mvn sonar:sonar \
  -Dsonar.projectKey=YOUR_PROJECT_KEY \
  -Dsonar.organization=YOUR_ORGANIZATION \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=YOUR_SONAR_TOKEN
```

## 🔄 Automated Analysis

The GitHub Actions workflow will automatically:

1. Trigger on every push to `main`, `develop`, and version branches
2. Trigger on every pull request
3. Build the project with Maven
4. Generate code coverage reports using JaCoCo
5. Upload results to SonarQube Cloud
6. Optionally upload to Codecov

## 📊 Viewing Results

1. After the workflow runs, go to your [SonarQube Cloud project](https://sonarcloud.io)
2. You'll see:
   - Code Quality metrics
   - Code Coverage percentage
   - Security issues
   - Code smells
   - Technical debt

## 🔐 Security Notes

- **Never commit your SONAR_TOKEN** to version control
- Always use GitHub Secrets for sensitive data
- Tokens can be regenerated from SonarQube Cloud if compromised
- Rotate tokens periodically

## ⚙️ Configuration Details

### Coverage Reporting
- JaCoCo is configured for code coverage
- Coverage reports are generated during `mvn test` phase
- Results are sent to SonarQube Cloud for analysis

### Exclusions
The following are excluded from analysis:
- Test files: `**/*test/**`, `**/*Test.java`
- Build artifacts: `**/target/**`
- Dependencies: `**/node_modules/**`

### Java Configuration
- Java Source Level: 21
- Java Target Level: 21
- Default Encoding: UTF-8

## 🐛 Troubleshooting

### Issue: "Project not found on SonarQube Cloud"
- **Solution**: Verify your SONAR_PROJECT_KEY and SONAR_ORGANIZATION are correct
- Check in SonarQube Cloud project settings

### Issue: "Authentication failed"
- **Solution**: Check your SONAR_TOKEN is valid
- Regenerate a new token in SonarQube Cloud

### Issue: "No code coverage data"
- **Solution**: Ensure tests are running correctly
- Check `mvn clean test` generates coverage reports

### Issue: Workflow fails at analysis step
- **Solution**: 
  1. Check GitHub Actions logs
  2. Verify all secrets are set correctly
  3. Ensure pom.xml files are valid XML

## 📚 Additional Resources

- [SonarQube Cloud Documentation](https://docs.sonarsource.com/sonarqube-cloud/)
- [SonarQube Maven Plugin](https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner-for-maven/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

## ✅ Checklist

- [ ] SonarQube Cloud account created
- [ ] Project created in SonarQube Cloud
- [ ] SONAR_TOKEN generated and stored
- [ ] GitHub Secrets configured (SONAR_TOKEN, SONAR_PROJECT_KEY, SONAR_ORGANIZATION)
- [ ] sonar-project.properties updated with your values
- [ ] Root pom.xml updated with your values
- [ ] Service pom.xml files updated with parent reference
- [ ] Workflow file is in `.github/workflows/sonarcloud.yml`
- [ ] First commit pushed to trigger workflow
- [ ] Results visible in SonarQube Cloud
