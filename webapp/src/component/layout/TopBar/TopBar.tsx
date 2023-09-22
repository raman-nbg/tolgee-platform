import { Link } from 'react-router-dom';
import { Box, IconButton, styled } from '@mui/material';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import { DarkMode, LightMode } from '@mui/icons-material';

import { LocaleMenu } from '../../LocaleMenu';
import { UserMenu } from '../../security/UserMenu/UserMenu';
import { useConfig } from 'tg.globalContext/helpers';
import { TolgeeLogo } from 'tg.component/common/icons/TolgeeLogo';
import { useThemeContext } from '../../../ThemeProvider';
import { AdminInfo } from './AdminInfo';
import {
  useGlobalActions,
  useGlobalContext,
} from 'tg.globalContext/GlobalContext';
import { RocketIcon } from 'tg.component/CustomIcons';

export const TOP_BAR_HEIGHT = 52;

const StyledAppBar = styled(AppBar)(
  ({ theme }) =>
    ({
      zIndex: theme.zIndex.drawer + 1,
      transition: 'transform 0.2s ease-in-out',
      ...theme.mixins.toolbar,
      background: theme.palette.navbarBackground.main,
      boxShadow:
        theme.palette.mode === 'dark' ? 'none' : theme.mixins.toolbar.boxShadow,
    } as any)
);

const StyledToolbar = styled(Toolbar)`
  padding-right: 12.5px !important;
  padding-left: 12.5px !important;
`;

const StyledVersion = styled(Typography)`
  margin-left: ${({ theme }) => theme.spacing(2)};
  font-size: 11px;
`;

const StyledLogoTitle = styled(Typography)`
  font-size: 20px;
  font-weight: 500;
  font-family: Righteous, Rubik, Arial, sans-serif;
  transition: filter 0.2s ease-in-out;
`;

const StyledLogoWrapper = styled(Box)`
  transition: filter 0.2s ease-in-out;
`;

const StyledTolgeeLink = styled(Link)`
  color: inherit;
  text-decoration: inherit;
  outline: 0;

  &:focus ${StyledLogoWrapper} {
    filter: brightness(95%);
  }
`;

const StyledIconButton = styled(IconButton)`
  width: 40px;
  height: 40px;
`;

type Props = {
  isAdminAccess?: boolean;
  isDebuggingCustomerAccount?: boolean;
};

export const TopBar: React.FC<Props> = ({
  isAdminAccess = false,
  isDebuggingCustomerAccount = false,
}) => {
  const config = useConfig();

  const topBarHidden = useGlobalContext((c) => !c.topBarHeight);
  const topBannerSize = useGlobalContext((c) => c.topBannerHeight);
  const guideOpen = useGlobalContext((c) => c.quickStartGuide.open);
  const { setRightPanelOpen } = useGlobalActions();

  const { mode, setMode } = useThemeContext();

  const toggleTheme = () => {
    if (mode === 'dark') {
      setMode('light');
    } else {
      setMode('dark');
    }
  };

  return (
    <StyledAppBar
      sx={{
        top: topBannerSize,
        transform: topBarHidden
          ? `translate(0px, -55px)`
          : `translate(0px, 0px)`,
      }}
    >
      <StyledToolbar>
        <Box flexGrow={1} display="flex">
          <Box>
            <StyledTolgeeLink to={'/'}>
              <Box display="flex" alignItems="center">
                <StyledLogoWrapper pr={1} display="flex" justifyItems="center">
                  <TolgeeLogo fontSize="large" />
                </StyledLogoWrapper>
                <StyledLogoTitle variant="h5" color="inherit">
                  {config.appName}
                </StyledLogoTitle>
                {config.showVersion && (
                  <StyledVersion variant="body1">
                    {config.version}
                  </StyledVersion>
                )}
              </Box>
            </StyledTolgeeLink>
          </Box>
          <AdminInfo
            adminAccess={isAdminAccess}
            debuggingCustomerAccount={isDebuggingCustomerAccount}
          />
        </Box>
        {guideOpen && (
          <StyledIconButton
            color="inherit"
            onClick={() => setRightPanelOpen((val) => !val)}
          >
            <RocketIcon fontSize="small" />
          </StyledIconButton>
        )}
        <StyledIconButton onClick={toggleTheme} color="inherit">
          {mode === 'dark' ? <LightMode /> : <DarkMode />}
        </StyledIconButton>
        <LocaleMenu />
        <UserMenu />
      </StyledToolbar>
    </StyledAppBar>
  );
};
